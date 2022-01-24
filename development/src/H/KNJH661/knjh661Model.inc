<?php

require_once('for_php7.php');

/**
 * 定数定義
*/
class knjh661Model extends Model
{
    public $cmd;
    public $control;
    public $file;
    public $field = array();
    public $data_arr = array();
    public $z010Name1;

    /**
     * メンバ変数の初期化
     */
    public function knjh661Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }
    public function init()
    {
        $this->cmd = VARS::request("cmd");
        $this->field = array("OUTPUT"           =>  VARS::post("OUTPUT"),           //出力取込種別
                             "HEADER"           =>  VARS::post("HEADER"),           //ヘッダ有
                             "SHORI_MEI"        =>  VARS::post("SHORI_MEI")         //処理名
                             );

        if (VARS::file("FILE")) {
            $this->field["FILE"] = VARS::file("FILE");      //ファイルのアップロード
            $this->field["FILE"]["name"] = mb_convert_encoding($this->field["FILE"]["name"], "SJIS-win", "UTF-8");
        }
        $this->getPropertiesAll();
    }

    /********************/
    /*  ＣＳＶ取込処理  */
    /********************/
    public function &getExecModel()
    {
        //ファイルの文字列の取得
        $this->data_arr = array();
        $this->data_arr = $this->getFileString();

        //データの整合性チェック
        if ($this->IsDataMatched($this->data_arr)) {
            $errFlg = $this->checkCsvErrQuery(PROGRAMID);
            if ($errFlg) {
                knjh661Query::updateQueryCsv($this, $this->data_arr);
                $this->setMessage("MSG202", "更新件数".get_count($this->data_arr)."件");
            }
        }
    }

    /*************************/
    /*  CSVファイルを配列へ  */
    /*************************/
    public function getFileString()
    {
        $temp_arr = array();
        if (!is_uploaded_file($this->field["FILE"]["tmp_name"])) { //HTTP POSTによりアップロードされたファイルかどうかを調べる
            $this->warning = "ファイル名が不正です。";
        } else {
            $checkdata = array();
            $db = Query::dbCheckOut();

            //科目
            $this->subclassArray = array();
            $query = knjh661Query::getTitle($this);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $this->subclassArray[] = $row;
            }
            $result->free();

            $checkdata["SCHREGNO"]              = array(8,    "HISSU",  "INT2",  "学籍番号");   //0
            $checkdata["BUNRIDIV"]              = array(1,    "HISSU",  "INT",   "文理CD");     //3
            foreach ($this->subclassArray as $key => $val) {
                $subclassCd = $val["CENTER_SUBCLASS_CD"];
                //国語合計
                if ($subclassCd == "000000") {
                    //得点
                } else {
                    $checkdata["SCORE".$subclassCd]     = array(3,    "",       "INT",   $val["SUBCLASS_ABBV"]);
                }
                //第1選択フラグ(社会と理科２のみ)
                if ($val["CENTER_CLASS_CD"] == "04" || $val["CENTER_CLASS_CD"] == "06") {
                    $checkdata["CHECK".$subclassCd]     = array(1,    "FLG",    "INT",   $val["SUBCLASS_ABBV"]."フラグ");
                }
            }

            $db->autoCommit(false);

            //エラーデータの削除
            $query = $this->deleteQueryErr(PROGRAMID);
            $result = $db->query($query);

            $fp = fopen($this->field["FILE"]["tmp_name"], "r");
            $i  = 0;
            $line = 1;  //行数
            $header = false;
            $primaryKey1   = array();
            $primaryKey2   = array();
            $duplicateCsv1 = array();
            $duplicateCsv2 = array();
            $primaryKey1   = $this->getPrimaryKey("MOCK_CENTER_DAT");
            $primaryKey2   = $this->getPrimaryKey("MOCK_CENTER_RANK_RANGE_DAT");
            while (!feof($fp)) {
                $buffer = fgets($fp, 4096);
                if (trim($buffer) != '') {
                    if (!$header && $this->field["HEADER"] == "on") {
                        $header = true;
                        $line++;
                        continue;
                    }
                    //文字コードを判別、改行コードを削除
                    $buffer = trim(mb_convert_encoding($buffer, mb_http_output(), "SJIS-win"));
                    $tmp    = common::csv2array($buffer);
                    //配列の要素数チェック
                    $checkcount = get_count($tmp);

                    $data = array();

                    $data["SCHREGNO"]               = $tmp[0];
                    $data["BUNRIDIV"]               = $tmp[3];

                    $tmpCnt = 4;
                    foreach ($this->subclassArray as $key => $val) { //$tmp[5]～
                        $subclassCd = $val["CENTER_SUBCLASS_CD"];
                        //国語合計
                        if ($subclassCd == "000000") {
                            $tmpCnt++;
                        //得点
                        } else {
                            $data["SCORE".$subclassCd]     = $tmp[++$tmpCnt];
                        }
                        //第1選択フラグ(社会と理科２のみ)
                        if ($val["CENTER_CLASS_CD"] == "04" || $val["CENTER_CLASS_CD"] == "06") {
                            $data["CHECK".$subclassCd]     = $tmp[++$tmpCnt];
                        }
                        $data["CENTER_CLASS_CD"]    = $val["CENTER_CLASS_CD"];
                        $data["CENTER_SUBCLASS_CD"] = $subclassCd;
                    }
                    $data["YEAR"]                   = CTRL_YEAR;
                    $data["CENTERCD"]               = "000000000";
                    $data["RANK_RANGE"]             = "1";
                    $data["RANK_DIV"]               = "02";
                    $data["CENTERDIV"]              = "1";

                    $dataCnt = 1 + $tmpCnt + 12;

                    //項目数によるファイルチェック
                    if ($checkcount != $dataCnt) {
                        $this->warning = "CSVファイルを確認して下さい。\\nフォーマットが一致していません。";
                        return;
                    }

                    //エラーチェック
                    if ($this->errCheck($db, $data, $checkdata, $line, $duplicateCsv1, $duplicateCsv2, $primaryKey1, $primaryKey2)) {
                        foreach ($checkdata as $key => $val) {
                            //ゼロ埋めしてセット
                            if ($val[2] == "INT2" && $data[$key] != "" && is_numeric($data[$key])) {
                                $data[$key] = sprintf("%0{$val[0]}d", $data[$key]);
                            }
                        }
                        $temp_arr[$i] = $data;
                        $i++;
                    }
                    foreach ($primaryKey1 as $key => $val) {
                        $duplicateCsv1[$line][$val] = $data[$val];
                    }
                    foreach ($primaryKey2 as $key => $val) {
                        $duplicateCsv2[$line][$val] = $data[$val];
                    }
                    $line++;
                }
            }
            $db->commit(); // トランザクションをコミットする。
            Query::dbCheckIn($db);
            fclose($fp);
        }

        return $temp_arr;
    }

    /********************/
    /*  エラーチェック  */
    /********************/
    public function errCheck($db, $data, $checkdata, $line, $duplicateCsv1, $duplicateCsv2, $primaryKey1, $primaryKey2)
    {
        $returnFlg          = true;
        $hissuErrFlg        = false;    //必須エラーフラグ
        $masterErrFlg       = false;    //存在エラーフラグ
        $numericErrFlg      = false;    //数字エラーフラグ
        $lengthErrFlg       = false;    //レングスエラーフラグ
        $valuesErrFlg       = false;    //その他エラーフラグ
        $duplicateCsvErrFlg = false;

        $hissuErrMsg        = "";       //必須エラーメッセージ
        $masterErrMsg       = "";       //存在エラーメッセージ
        $numericErrMsg      = "";       //数字エラーメッセージ
        $lengthErrMsg       = "";       //レングスエラーメッセージ
        $valuesErrMsg       = "";       //その他エラーメッセージ
        $duplicateCsvErrMsg = "";

        $hissuSep           = "";
        $masterSep          = "";
        $numericSep         = "";
        $lengthSep          = "";
        $valuesSep          = "";
        $duplicateCsvSep    = "";

        $dupFlg1 = $this->duplicateCsvCheck($duplicateCsv1, $primaryKey1, $data);
        if ($dupFlg1[0] == 1) {
            $returnFlg = $this->errSet($duplicateCsvErrFlg, $duplicateCsvErrMsg, "※重複(CSV):", $duplicateCsvSep, "重複1(". $dupFlg1[1] ."行目)");
        }
        $dupFlg2 = $this->duplicateCsvCheck($duplicateCsv2, $primaryKey2, $data);
        if ($dupFlg2[0] == 1) {
            $returnFlg = $this->errSet($duplicateCsvErrFlg, $duplicateCsvErrMsg, "※重複(CSV):", $duplicateCsvSep, "重複2(". $dupFlg2[1] ."行目)");
        }

        $checkNameMstCd = array("BUNRIDIV"      => "H316"
                                );

        foreach ($checkdata as $key => $val) {
            //必須項目チェック
            if ($val[1] == "HISSU" && !strlen(trim($data[$key]))) {
                $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[3]);
            }

            //数値チェック
            if (($val[2] == "INT" || $val[2] == "INT2") && $data[$key] != "" && !is_numeric($data[$key])) {
                $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, $val[3]);
            }
            //チェック用にゼロ埋め
            if ($val[2] == "INT2" && $data[$key] != "" && is_numeric($data[$key])) {
                $data[$key] = sprintf("%0{$val[0]}d", $data[$key]);
            }

            //存在チェック（名称マスタ）
            if ($checkNameMstCd[$key] && $data[$key] != "") {
                $query = knjh661Query::nameCntSql($checkNameMstCd[$key], $data[$key]);
                $rowCount = $db->getOne($query);
                if ($rowCount == "0") {
                    $returnFlg = $this->errSet($masterErrFlg, $masterErrMsg, "※マスタチェック:", $masterSep, $val[3]);
                }
            }
            //フラグチェック(値は1かNULL)
            if ($val[1] == "FLG" && $data[$key] != "" && $data[$key] != "1") {
                $returnFlg = $this->errSet($valuesErrFlg, $valuesErrMsg, "※値不正:", $valuesSep, $val[3]);
            }

            //文字数チェック
            if (strlen($data[$key]) > $val[0]) {
                $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[3]);
            }
        }

        if (!$returnFlg) {
            $checkError = $duplicateCsvErrMsg.$hissuErrMsg.$numericErrMsg.$lengthErrMsg.$masterErrMsg.$valuesErrMsg;
            $checkError = $this->checkErrLength($checkError);
            knjh661Query::insertQueryErr($db, $line, $checkError);
        }
        return $returnFlg;
    }

    /****************************/
    /*  データの整合性チェック  */
    /****************************/
    public function isDataMatched(&$data_arr)
    {
        //５千件を超えたら制限エラー
        if (get_count($data_arr) > 5000) {
            $this->setWarning("MSG915", "処理件数を５千件以下にして下さい 処理件数：".get_count($data_arr)."件");
            return false;
        }
        return true;
    }

    //CSV出力(ヘッダ)
    public function outputDataFileHead()
    {
        //DB接続
        $db = Query::dbCheckOut();

        $contents = "";

        //ヘッダ
        $contents .= implode(",", $this->getHeadArray($db))."\n";
        //ヘッダ例
        $contents .= implode(",", $this->getExampleArray($db))."\n";

        //DB切断
        Query::dbCheckIn($db);

        common::downloadContents($contents, "センター試験(ヘッダ).csv");

        return true;
    }

    //CSV出力(エラー)
    public function outputDataFileError()
    {
        //DB接続
        $db = Query::dbCheckOut();

        $contents = "";

        $headerDuplicate = array("YEAR"               => "年度",
                                 "CENTERCD"           => "センターコード(未使用)",
                                 "SCHREGNO"           => "学籍番号",
                                 "CENTER_CLASS_CD"    => "共通テスト教科コード",
                                 "CENTER_SUBCLASS_CD" => "共通テスト科目コード",
                                 "RANK_RANGE"         => "順位範囲",
                                 "RANK_DIV"           => "集計範囲",
                                 "CENTERDIV"          => "順位使用項目");
        $tables = array();
        $tables = array("1" => "MOCK_CENTER_DAT", "2" => "MOCK_CENTER_RANK_RANGE_DAT");
        $dupTable = $this->getDuplicateErrorHeader(PROGRAMID, $tables);
        if (sizeof($dupTable) > 0) {
            $headerErr = $this->duplicateCsvHeader($dupTable, $headerDuplicate);
            $contents .= implode($headerErr, ",")."\n";
        }
        //ヘッダ
        $contents .= "行番号, メッセージ\n";

        $header_data = false;
        //SQL文発行
        $query = $this->selectCsvErrQuery(PROGRAMID);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $row = str_replace(",", ".", $row);
            $contents .= implode($row, ",") ."\n";
            $header_data = true;
        }
        $result->free();

        //DB切断
        Query::dbCheckIn($db);
        //ＣＳＶ出力
        if ($header_data) {
            common::downloadContents($contents, "センター試験(エラー).csv");

            return true;
        } else {
            $this->warning = "該当データは、ありません。";
            return false;
        }
    }

    //CSV出力(データ)
    public function outputDataFile()
    {
        //DB接続
        $db = Query::dbCheckOut();

        $contents = "";

        //ヘッダ
        $contents .= implode(",", $this->getHeadArray($db))."\n";

        $header_data = false;
        //SQL文発行
        $query = knjh661Query::getselectCenterData($this);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $row = str_replace(",", ".", $row);
            if ($row["EIGO_GOUKEI"]) {
                $row["EIGO_GOUKEI"] = $row["EIGO_GOUKEI"] * 1;
            }
            if ($row["KOKUGO_GOUKEI"]) {
                $row["KOKUGO_GOUKEI"] = $row["KOKUGO_GOUKEI"] * 1;
            }
            if ($row["SUUGAKU_GOUKEI"]) {
                $row["SUUGAKU_GOUKEI"] = $row["SUUGAKU_GOUKEI"] * 1;
            }
            if ($row["RIKA_GOUKEI"]) {
                $row["RIKA_GOUKEI"] = $row["RIKA_GOUKEI"] * 1;
            }
            if ($row["SOU_GOUKEI"]) {
                $row["SOU_GOUKEI"] = $row["SOU_GOUKEI"] * 1;
            }
            if ($row["TOTAL_PERFECT"] == "0") {
                $row["TOTAL_PERFECT"] = "";
            }
            $contents .= implode($row, ",") ."\n";
            $header_data = true;
        }
        $result->free();

        //DB切断
        Query::dbCheckIn($db);
        //ＣＳＶ出力
        if ($header_data) {
            common::downloadContents($contents, "センター試験.csv");

            return true;
        } else {
            $this->warning = "該当データは、ありません。";
            return false;
        }
    }

    public function getHeadArray($db)
    {
        $retHeadArray = array();
        $retHeadArray[] = "学籍番号";
        $retHeadArray[] = "年組番";
        $retHeadArray[] = "氏名";
        $retHeadArray[] = "文理CD";
        $retHeadArray[] = "文理";
        //科目
        $this->subclassArray = array();
        $query = knjh661Query::getTitle($this);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $retHeadArray[] = $row["SUBCLASS_ABBV"];
            $this->subclassArray[] = $row;
            //社会と理科２
            if ($row["CENTER_CLASS_CD"] == "04" || $row["CENTER_CLASS_CD"] == "06") {
                $retHeadArray[] = $row["SUBCLASS_ABBV"]."フラグ";
            }
        }
        $result->free();

        $retHeadArray[] = "英語合計";
        $retHeadArray[] = "数学合計";
        $retHeadArray[] = "地歴合計";
        $retHeadArray[] = "公民合計";
        $retHeadArray[] = "理科合計";
        $retHeadArray[] = "総合計";
        $retHeadArray[] = "満点";
        $retHeadArray[] = "合計8";
        $retHeadArray[] = "合計5";
        $retHeadArray[] = "順位8";
        $retHeadArray[] = "順位5";
        $retHeadArray[] = "入試番号";

        return $retHeadArray;
    }

    //ヘッダ例
    public function getExampleArray($db)
    {
        $retHeadArray = array();
        $retHeadArray[] = "99999999";
        $retHeadArray[] = "表示のみ";
        $retHeadArray[] = "表示のみ";
        $retHeadArray[] = "1:文 2:理";
        $retHeadArray[] = "文";
        //科目
        $this->subclassArray = array();
        $query = knjh661Query::getTitle($this);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $retHeadArray[] = "100";
            $this->subclassArray[] = $row;
            //社会と理科２
            if ($row["CENTER_CLASS_CD"] == "04" || $row["CENTER_CLASS_CD"] == "06") {
                $retHeadArray[] = "1:第1選択";
            }
        }
        $result->free();

        $retHeadArray[] = "表示のみ";
        $retHeadArray[] = "表示のみ";
        $retHeadArray[] = "表示のみ";
        $retHeadArray[] = "表示のみ";
        $retHeadArray[] = "表示のみ";
        $retHeadArray[] = "表示のみ";
        $retHeadArray[] = "表示のみ";
        $retHeadArray[] = "空白";
        $retHeadArray[] = "空白";
        $retHeadArray[] = "空白";
        $retHeadArray[] = "空白";
        $retHeadArray[] = "空白";

        return $retHeadArray;
    }
}
