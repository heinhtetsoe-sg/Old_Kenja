<?php

require_once('for_php7.php');
/**
 * 定数定義
*/
class knjx130Model extends Model
{
    public $field      = array();
    public $data       = array();
    public $cmd;
    public $control;           //コントロールマスタ
    public $staffcd;           //登録者コード
    public $lastColumn;//CSVの最後の列の値

    /**
     * メンバ変数の初期化
     */
    public function knjx130Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd = STAFFCD;                           //登録者コード
    }
    public function init()
    {
        $this->field = array("OUTPUT"           =>  VARS::post("OUTPUT"),           //出力取込種別
                             "EXECUTEDATE"      =>  VARS::post("EXECUTEDATE"),      //実施日付一覧
                             "CHAIRCD"          =>  VARS::post("CHAIRCD"),          //講座一覧
                             "HEADER"           =>  VARS::post("HEADER"),           //ヘッダ有
                             "SHORI_MEI"        =>  VARS::post("SHORI_MEI")         //処理名
                            );

        $this->cmd          = VARS::request("cmd");
        if (VARS::file("FILE")) {
            $this->field["FILE"] = VARS::file("FILE");        //ファイルのアップロード
            $this->field["FILE"]["name"] = i18n_convert($this->field["FILE"]["name"], "UTF-8", "SJIS-win");
        }

        $this->getPropertiesAll(); //プロパティファイル
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
        $this->lastColumn = 'DUMMY';
        if ($this->Properties["csv_LastColumn"] != '') {
            $this->lastColumn = $this->Properties["csv_LastColumn"];
        }
    }
    public function &getMainModel()
    {
    }

    /**
     * 実行モデルを返します
    */
    public function &getExecModel()
    {
        //ファイル取り込み
        $this->dataArr = array();
        $this->dataArr = $this->fileUpload();

        //データの整合性チェック
        if ($this->isDataMatched($this->dataArr)) {
            $errFlg = $this->checkCsvErrQuery(PROGRAMID);
            if ($errFlg) {
                $db = Query::dbCheckOut();
                $db->autoCommit(false);
                for ($i = 0; $i < get_count($this->dataArr); $i++) {
                    //時間割講座データ
                    $query = knjx130Query::getExistCheck($this->dataArr[$i]);
                    $result = $db->query($query);
                    $flg = false;
                    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                        $flg = true;
                    }
                    //更新
                    if ($this->field["SHORI_MEI"]=="1") {
                        if ($flg) {   //ＤＢにあれば、update
                            knjx130Query::updateQueryCsv($db, $this->dataArr[$i], $this->staffcd);
                        } else {    //ＤＢになければ、insert
                            knjx130Query::insertQueryCsv($db, $this->dataArr[$i], $this->staffcd);
                        }
                        //削除
                    } else {
                        //ＤＢにあれば、delete
                        if ($flg) {
                            knjx130Query::deleteQueryCsv($db, $this->dataArr[$i]);
                        }
                    }
                }
                $db->commit();
                Query::dbCheckIn($db);
                $this->setMessage("MSG202", "更新件数".get_count($this->dataArr)."件");
            }
        }
    }

    //取込
    public function fileUpload()
    {
        if (is_uploaded_file($this->field["FILE"]["tmp_name"])) {       //HTTP POSTによりアップロードされたファイルかどうかを調べる
            $checkdata = array("EXECUTEDATE"   => array(10, "実施日付"),
                               "PERIODCD"      => array(1, "校時コード"),
                               "CHAIRCD"       => array(7, "講座コード"),
                               "SCHOOLING_SEQ" => array(99, "スクーリング回数"),
                               "ATTESTOR"      => array(8, "出欠確認者コード"),
                               "REMARK"        => array(40, "備考"));
            $db = Query::dbCheckOut();
            $db->autoCommit(false);

            //エラーデータの削除
            $query = $this->deleteQueryErr(PROGRAMID);
            $result = $db->query($query);

            $i = 0;
            $record_no = 1;         //行番号(初期値)
            $primaryKey1 = array();
            $duplicateCsv1 = array();
            $primaryKey1 = $this->getPrimaryKey('SCH_CHR_T_DAT');

            $fp = fopen($this->field["FILE"]["tmp_name"], "r");        //ファイルまたはURLをオープンする
            while (!feof($fp)) {                                       //ファイルポインタがファイル終端に達しているかどうか調べる
                $buffer = fgets($fp, 4096);                             //ファイルポインタから1行取得する
                if (trim($buffer) != '') {
                    //ヘッダ有なら１行目は無視
                    if ($this->field["HEADER"]=="on" && $record_no<2) {
                        $record_no++;
                        continue;
                    }
                    $buffer = i18n_convert($buffer, "UTF-8", "SJIS-win");     //文字列を指定コードに変換する

                    $tmp = explode(",", $buffer);

                    $data = array("EXECUTEDATE"     => trim($tmp[0]),
                                  "PERIODCD"        => trim($tmp[1]),
                                  "CHAIRCD"         => trim($tmp[2]),
                                  "SCHOOLING_SEQ"   => trim($tmp[3]),
                                  "EXECUTED"        => "0",
                                  "DATADIV"         => "0",
                                  "SEMESTER"        => "0",             //実施日付より学期を算出しセットする
                                  "ATTESTOR"        => trim($tmp[4]),
                                  "REMARK"          => trim($tmp[5]));

                    //日付変換
                    $data_check = $this->dateRep($data["EXECUTEDATE"]);
                    if ($data_check) {
                        $data["EXECUTEDATE"] = $data_check;
                    }
                    //０埋め
                    $data = $this->zeroUme($data);

                    //実施日付より学期を算出しセットする
                    if ($data["EXECUTEDATE"] != "" && $data_check) {
                        $query = knjx130Query::getSemesterSQL($data);
                        $result = $db->query($query);
                        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                            $data["SEMESTER"] = $row["SEMESTER"];
                        }
                    }
                    //エラーチェック
                    if ($this->errCheck($db, $data, $checkdata, $record_no, $duplicateCsv1, $primaryKey1)) {
                        $tempArr[$i] = $data;
                        $i++;
                    }
                    foreach ($primaryKey1 as $key => $val) {
                        $duplicateCsv1[$record_no][$val] = $data[$val];
                    }
                }//ifの綴り
                $record_no++;   //行番号(カウント)
            }//whileの綴り
            $db->commit(); // トランザクションをコミットする。
            Query::dbCheckIn($db);
            fclose($fp);
        } else {
            $this->warning = "ファイル名が不正です。";
        }
        return $tempArr;
    }

   //エラーチェック
    public function errCheck($db, $data, $checkdata, $line, $duplicateCsv1, $primaryKey1)
    {
        $returnFlg          = true;
        $hissuErrFlg        = false;    //必須エラーフラグ
        $numericErrFlg      = false;    //数値エラーフラグ
        $lengthErrFlg       = false;    //レングスエラーフラグ
        $masterErrFlg       = false;    //マスタエラーフラグ
        $existenceErrFlg    = false;    //存在エラーフラグ
        $dateErrFlg         = false;    //日付エラーフラグ
        $duplicateCsvErrFlg = false;    //重複(CSV)エラーフラグ

        $hissuErrMsg        = "";     //必須エラーメッセージ
        $numericErrMsg      = "";     //数値エラーメッセージ
        $lengthErrMsg       = "";     //レングスエラーメッセージ
        $masterErrMsg       = "";     //マスタエラーメッセージ
        $existenceErrMsg    = "";     //存在エラーメッセージ
        $dateErrMsg         = "";     //日付エラーメッセージ
        $duplicateCsvErrMsg = "";     //重複(CSV)メッセージ

        $hissuSep        = "";
        $numericSep      = "";
        $lengthSep       = "";
        $masterSep       = "";
        $existenceSep    = "";
        $dateSep         = "";
        $duplicateCsvSep = "";

        //重複(CSV)チェック
        $dupFlg1 = $this->duplicateCsvCheck($duplicateCsv1, $primaryKey1, $data);
        if ($dupFlg1[0] == 1) {
            $returnFlg = $this->errSet($duplicateCsvErrFlg, $duplicateCsvErrMsg, "※重複(CSV):", $duplicateCsvSep, "重複1(". $dupFlg1[1] ."行目)");
        }
        foreach ($checkdata as $key => $val) {
            //必須チェック
            if (in_array($key, array("EXECUTEDATE", "PERIODCD", "CHAIRCD", "SCHOOLING_SEQ"))) {
                if (!strlen(trim($data[$key]))) {
                    $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[1]);
                }
            }
            //文字数チェック
            if (strlen($data[$key]) > $val[0]) {
                $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[1]);
            }
            //桁数チェック
            if (in_array($key, array("CHAIRCD", "SCHOOLING_SEQ"))) {
                if ($data[$key] != "" && !is_numeric($data[$key])) {
                    $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, $val[1]);
                }
            }
        }
        if ($returnFlg) {
            if ($data["EXECUTEDATE"] != "") {
                $data_check = $this->dateRep($data["EXECUTEDATE"]);
                if (!$data_check) {
                    $returnFlg = $this->errSet($dateErrFlg, $dateErrMsg, "※日付:", $dateSep, "実施日付");
                }
            }
            if ($this->field["SHORI_MEI"] == "1") {
                //職名マスタ
                if ($data["EXECUTEDATE"] != "" && $data_check && $data["CHAIRCD"] != "") {
                    $result = $db->getOne(knjx130Query::getMasterCheck($data, "1"));
                    if ($result != "X") {
                        $returnFlg = $this->errSet($masterErrFlg, $masterErrMsg, "※マスタチェック:", $masterSep, "講座コード");
                    }
                }
                if ($data["ATTESTOR"] != "") {
                    $result = $db->getOne(knjx130Query::getMasterCheck($data, "2"));
                    if ($result != "X") {
                        $returnFlg = $this->errSet($masterErrFlg, $masterErrMsg, "※マスタチェック:", $masterSep, "出欠確認者コード");
                    }
                }
                if ($data["PERIODCD"] != "") {
                    $result = $db->getOne(knjx130Query::getMasterCheck($data, "3"));
                    if ($result != "X") {
                        $returnFlg = $this->errSet($masterErrFlg, $masterErrMsg, "※マスタチェック:", $masterSep, "校時コード");
                    }
                }
            }
        }
        if ($returnFlg) {
            //時間割講座データ
            $result = $db->query(knjx130Query::getExistCheck($data));
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                if ($this->field["SHORI_MEI"] == "1") {
                    if ($row["EXECUTED"] == "1") {
                        $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, "更新不可");
                    }
                } //実施区分のチェック用
            }
        }
        if (!$returnFlg) {
            $checkError = $duplicateCsvErrMsg.$hissuErrMsg.$numericErrMsg.$lengthErrMsg.$dateErrMsg.$masterErrMsg.$existenceErrMsg;
            $checkError = $this->checkErrLength($checkError);
            knjx130Query::insertQueryErr($db, $line, $checkError);
        }
        return $returnFlg;
    }

    //ＣＳＶ出力
    public function getDownloadModel()
    {
        //項目
        $header_err = array("MSGROW"            => "行番号",
                            "MSGREMARK"         => "メッセージ");
        $header = array("EXECUTEDATE"     => "※実施日付",
                        "PERIODCD"        => "※校時コード",
                        "CHAIRCD"         => "※講座コード",
                        "SCHOOLING_SEQ"   => "※スクーリング回数",
                        "ATTESTOR"        => "出欠確認者コード",
                        "REMARK"          => "備考(全角20文字まで)",
                        $this->lastColumn => $this->lastColumn);
        $header_rei = array("EXECUTEDATE"     => "2004-05-01",
                            "PERIODCD"        => "4",
                            "CHAIRCD"         => "9999999",
                            "SCHOOLING_SEQ"   => "99",
                            "ATTESTOR"        => "88888888",
                            "SCHOOLING_SEQ"   => "99",
                            "REMARK"          => "展開と因数分解",
                            $this->lastColumn => $this->lastColumn);

        $contents  = "";    //クリア

        if ($this->field["OUTPUT"]=="1") {             //ヘッダ
            $contents .= implode($header, ",")."\n";
            $contents .= implode($header_rei, ",")."\n";
        } elseif ($this->field["OUTPUT"]=="3") {     //エラー
            $tables = array();
            $tables = array("1" => "SCH_CHR_T_DAT");
            $dupTable = $this->getDuplicateErrorHeader(PROGRAMID, $tables);
            if (sizeof($dupTable) > 0) {
                $headerErr = $this->duplicateCsvHeader($dupTable, $header);
                $contents = implode($headerErr, ",")."\n";
            }
            $contents .= implode($header_err, ",")."\n";
        } elseif ($this->field["OUTPUT"]=="4") {     //データ
            if ($this->field["HEADER"]=="on") {
                $contents .= implode($header, ",")."\n";
            }
        }
        if ($this->field["OUTPUT"]=="1") {         //ヘッダ
            $header_data = true;
        } else {                                //ヘッダ以外
            $header_data = false;
            //DB接続
            $db = Query::dbCheckOut();
            $table = "";
            $where = "";
            if ($this->field["OUTPUT"]=="3") {             //エラー
                $query = $this->selectCsvErrQuery(PROGRAMID);
            } elseif ($this->field["OUTPUT"]=="4") {     //データ
                $query = knjx130Query::getselectDataSQL($this);
            }
            $limit_flg = 0;     //行数上限65536
            //SQL文発行
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $limit_flg++;
                if ($limit_flg > 65535) {
                    break;
                }
                if ($this->field["OUTPUT"]=="4") {                //データ
                    $row = str_replace(",", ".", $row);//データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
                }//if
                $contents .= implode($row, ",") ."\n";
                $header_data = true;
            }//while
            $result->free();
            Query::dbCheckIn($db);
        }
        //ＣＳＶ出力
        if ($header_data) {
            if ($this->field["OUTPUT"]=="3") {        //エラー
                $couse = "エラー出力";
            } else {                                //エラー以外
                $couse = "時間割講座データ";
            }
            $filename = sprintf("%s.csv", $couse);
            common::downloadContents($contents, $filename);
            return true;
        } else {
            $this->warning = "該当データは、ありません。";
            return false;
        }
    }

    /**
     * 日付変換 項目ﾁｪｯｸ後に行う。
     */
    public function dateRep($data)
    {
        if ($data!="") {
            $data = common::DateConv1(str_replace("-", "/", $data), 4);
            if ($data) {
                $data = str_replace("/", "-", $data);
            }
        }

        return $data;
    }

    /**
     * ０埋め 項目ﾁｪｯｸ後に行う。
     */
    public function zeroUme($data)
    {
        if ($data["CHAIRCD"]!="") {
            if (strlen($data["CHAIRCD"]) < 7) {
                $data["CHAIRCD"] = sprintf("%07d", $data["CHAIRCD"]);
            }
        }
        if ($data["ATTESTOR"]!="") {
            if (strlen($data["ATTESTOR"]) < 8) {
                $data["ATTESTOR"] = sprintf("%08d", $data["ATTESTOR"]);
            }
        }

        return $data;
    }

    //データの整合性チェック
    public function isDataMatched(&$dataArr)
    {
        //１万件を超えたら制限エラー
        if (get_count($dataArr) > 10000) {
            $this->setWarning("MSG915", "処理件数を１万件以下にして下さい 処理件数：".get_count($dataArr)."件");
            return false;
        }
        return true;
    }
}
