<?php

require_once('for_php7.php');
/**
 * 定数定義
*/
class knjx_h211Model extends Model
{
    public $field      = array();
    public $data       = array();
    public $cmd;
    public $file;
    public $control;           //コントロールマスタ
    public $staffcd;           //登録者コード
    public $sec_competence;
    public $getPrgId;  //コールされたか

    /**
     * メンバ変数の初期化
     */
    public function knjx_h211Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd = STAFFCD;                            //登録者コード
    }

    public function init()
    {
        if (VARS::get("PROGRAMID")) {
            unset($this->getPrgId);
        }
        if(VARS::get("SEND_PRGID")){
            $this->getPrgId = VARS::get("SEND_PRGID");
        }

        $this->field = array("GRADE"            =>  VARS::post("GRADE"),             //学年一覧
                             "OUTPUT"           =>  VARS::post("OUTPUT"),            //出力取込種別
                             "HEADER"           =>  VARS::post("HEADER"),            //ヘッダ有
                             "SHORI_MEI"        =>  VARS::post("SHORI_MEI")          //処理名
                             );

        $this->cmd = VARS::request("cmd");
        $this->file = VARS::file("FILE");

        //セキュリティーチェック
        $this->sec_competence = common::SecurityCheck($this->staffcd, $this->getPrgId);

        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"]) {
            $this->getSelectSchoolKind();
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
        // 権限チェック
        if ($this->sec_competence != DEF_UPDATABLE && $this->sec_competence != DEF_UPDATE_RESTRICT) {
            $this->setWarning("MSG300");
            return false;
        }

        //ファイルの文字列の取得
        $data_arr = array();
        $data_arr = $this->getFileString();

        //データの整合性チェック
        if ($this->isDataMatched($ata_arr)) {
            $errFlg = $this->checkCsvErrQuery(PROGRAMID);
            if ($errFlg) {
                if ($this->field["SHORI_MEI"] == "1") {
                    knjx_h211Query::insertQueryCsv($this, $data_arr);
                } else {
                    knjx_h211Query::deleteQueryCsv($this, $data_arr);
                }

                $this->setMessage("MSG202", "更新件数".get_count($data_arr)."件");
            }
        }
    }

    //取込
    public function getFileString()
    {
        $temp_arr = array();
        if (!is_uploaded_file($this->file["tmp_name"])) {        //HTTP POSTによりアップロードされたファイルかどうかを調べる
            $this->warning = "ファイル名が不正です。";
        } else {
            $checkdata = array();
            $checkdata["YEAR"]          = array(4,  "HISSU",  "INT",  "年度");
            $checkdata["SCHREGNO"]      = array(8,  "HISSU",  "INT",  "学籍番号");
            $checkdata["DOMI_CD"]       = array(3,  "HISSU",  "INT",  "寮コード");
            $checkdata["DOMI_ENTDAY"]   = array(10, "HISSU",  "DATE", "入寮日");
            $checkdata["DOMI_OUTDAY"]   = array(10, "",       "DATE", "退寮日");

            $db = Query::dbCheckOut();
            $db->autoCommit(false);

            //エラーデータの削除
            $query = $this->deleteQueryErr(PROGRAMID);
            $result = $db->query($query);

            $fp = fopen($this->file["tmp_name"], "r");
            $i  = 0;
            $line = 1;  //行数
            $header = false;
            $primaryKey1 = array();
            $duplicateCsv1 = array();
            $primaryKey1 = $this->getPrimaryKey('SCHREG_DOMITORY_HIST_DAT');
            while (!feof($fp)) {
                $buffer = fgets($fp, 4096);
                if (trim($buffer) != '') {
                    if (!$header && $this->field["HEADER"] == "on") {
                        $header = true;
                        $line++;
                        continue;
                    }
                    //文字コードを判別、改行コードを削除
                    $buffer = trim(i18n_convert($buffer, i18n_http_output(), "SJIS-win"));
                    $tmp    = common::csv2array($buffer);
                    $tmp    = str_replace("↓", "\n", $tmp);
                    //配列の要素数チェック
                    $checkcount = get_count($tmp);

                    $tmpcnt = 0;
                    $data = array();
                    $data["YEAR"]           = $tmp[$tmpcnt++];
                    $data["SCHREGNO"]       = $tmp[$tmpcnt++];
                    $data["GRADE"]          = $tmp[$tmpcnt++];
                    $data["HR_CLASS"]       = $tmp[$tmpcnt++];
                    $data["ATTENDNO"]       = $tmp[$tmpcnt++];
                    $data["NAME"]           = $tmp[$tmpcnt++];
                    $data["DOMI_CD"]        = $tmp[$tmpcnt++];
                    $data["DOMI_NAME"]      = $tmp[$tmpcnt++];
                    $data["DOMI_ENTDAY"]    = str_replace("/", "-", $tmp[$tmpcnt++]);
                    $data["DOMI_OUTDAY"]    = str_replace("/", "-", $tmp[$tmpcnt++]);

                    $dataCnt = get_count($data);

                    //項目数によるファイルチェック
                    if ($checkcount != $dataCnt) {
                        $this->warning = "CSVファイルを確認して下さい。\\nフォーマットが一致していません。";
                        return;
                    }

                    //エラーチェック
                    if ($this->errCheck($db, $data, $checkdata, $line, $duplicateCsv1, $primaryKey1)) {
                        $temp_arr[$i] = $data;
                        $i++;
                    }
                    foreach ($primaryKey1 as $key => $val) {
                        $duplicateCsv1[$line][$val] = $data[$val];
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

    //エラーチェック
    public function errCheck($db, $data, $data_each, $line, $duplicateCsv1, $primaryKey1)
    {
        $returnFlg          = true;
        $hissuErrFlg        = false;    //必須エラーフラグ
        $numericErrFlg      = false;    //数字エラーフラグ
        $lengthErrFlg       = false;    //レングスエラーフラグ
        $existenceErrFlg    = false;    //存在エラーフラグ
        $otherErrFlg        = false;
        $orderErrFlg        = false;
        $duplicateCsvErrFlg = false;  //重複エラーフラグ
        $duplicateDbErrFlg  = false;

        $hissuErrMsg        = "";       //必須エラーメッセージ
        $numericErrMsg      = "";       //数字エラーメッセージ
        $lengthErrMsg       = "";       //レングスエラーメッセージ
        $existenceErrMsg    = "";       //存在エラーメッセージ
        $otherErrMsg        = "";
        $orderErrMsg        = "";
        $duplicateCsvErrMsg = "";       //重複エラーメッセージ
        $duplicateDbErrMsg  = "";

        $hissuSep        = "";
        $existenceSep    = "";
        $numericSep      = "";
        $lengthSep       = "";
        $existenceSep    = "";
        $otherSep        = "";
        $orderSep        = "";
        $duplicateCsvSep = "";
        $duplicateDbSep  = "";

        $dupFlg1 = $this->duplicateCsvCheck($duplicateCsv1, $primaryKey1, $data);
        if ($dupFlg1[0] == 1) {
            $returnFlg = $this->errSet($duplicateCsvErrFlg, $duplicateCsvErrMsg, "※重複(CSV):", $duplicateCsvSep, "重複1(". $dupFlg1[1] ."行目)");
        }

        //重複日チェック
        $query = knjx_h211Query::checkDomitoryHistDat($this, $data);
        $histcheck = $db->getRow($query, DB_FETCHMODE_ASSOC);
        if (is_array($histcheck)) {
            $returnFlg = $this->errSet($duplicateDbErrFlg, $duplicateDbErrMsg, "※重複(登録済):", $duplicateDbSep, "重複1");
        }

        foreach ($data_each as $key => $val) {
            //必須項目チェック
            if ($val[1] == "HISSU" && !strlen(trim($data[$key]))) {
                $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[3]);
            }

            //数値チェック
            if (($val[2] == "INT" || $val[2] == "INT2") && $data[$key] != "" && !is_numeric($data[$key])) {
                $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, $val[3]);
            }

            //日付チェック
            if ($val[2] == "DATE" && $data[$key] != "" && $this->dateRep($data[$key]) == "") {
                $returnFlg = $this->errSet($otherErrFlg, $otherErrMsg, "※その他:", $otherSep, $val[3]);
            }

            if ($this->field["SHORI_MEI"] == "1") {
                //文字数チェック
                if (strlen($data[$key]) > $val[0]) {
                    $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[3]);
                }
            }
        }

        //存在チェック（在籍）
        if (1 > $db->getOne(knjx_h211Query::checkRegdDat($this, $data["YEAR"], $data["SCHREGNO"]))) {
            $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, "学籍番号");
        }

        //存在チェック（学籍番号）
        if (1 > $db->getOne(knjx_h211Query::checkRegdBaseMst($this, $data["SCHREGNO"]))) {
            $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, "学籍番号");
        }

        if ($this->field["SHORI_MEI"] == "1") {
            //存在チェック（寮コード）
            if (1 > $db->getOne(knjx_h211Query::checkDomiCd($this, $data["YEAR"], $data["DOMI_CD"]))) {
                $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, "寮コード");
            }

            //入寮日と退寮日の大小チェック
            if ($data["DOMI_OUTDAY"] && ($data["DOMI_ENTDAY"] > $data["DOMI_OUTDAY"])) {
                $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※日付の大小:", $numericSep, "入寮/退寮日の大小");
            }

            //入寮日チェック
            if ($data["DOMI_ENTDAY"] != "" && $this->dateRep($data["DOMI_ENTDAY"])) {
                $query = knjx_h211Query::checkRegdDatDate($this, $data["YEAR"], $data["SCHREGNO"], $this->dateRep($data["DOMI_ENTDAY"]));
                $histcheck = $db->getRow($query, DB_FETCHMODE_ASSOC);
                if (!is_array($histcheck)) {
                    $returnFlg = $this->errSet($orderErrFlg, $orderErrMsg, "※固有エラー:", $orderSep, "入寮日が該当生徒の在籍中でありません。");
                }
            }

            //退寮日チェック
            if ($data["DOMI_OUTDAY"] != "" && $this->dateRep($data["DOMI_OUTDAY"])) {
                $query = knjx_h211Query::checkRegdDatDate($this, $data["YEAR"], $data["SCHREGNO"], $this->dateRep($data["DOMI_OUTDAY"]));
                $histcheck = $db->getRow($query, DB_FETCHMODE_ASSOC);
                if (!is_array($histcheck)) {
                    $returnFlg = $this->errSet($orderErrFlg, $orderErrMsg, "※固有エラー:", $orderSep, "退寮日が該当生徒の在籍中でありません。");
                }
            }
        }

        if (!$returnFlg) {
            $checkError = $duplicateCsvErrMsg.$duplicateDbErrMsg.$hissuErrMsg.$numericErrMsg.$lengthErrMsg.$existenceErrMsg.$otherErrMsg.$orderErrMsg;
            $checkError = $this->checkErrLength($checkError);
            knjx_h211Query::insertQueryErr($db, $line, $checkError);
        }
        return $returnFlg;
    }

    //日付変換 項目チェック後に行う。
    public function dateRep($data)
    {
        if ($data != "") {
            $data = common::DateConv1(str_replace("-", "/", $data), 4);
            if ($data) {
                $data = str_replace("/", "-", $data);
            }
        }

        return $data;
    }

    //データの整合性チェック
    public function isDataMatched(&$data_arr)
    {
        //５千件を超えたら制限エラー
        if (get_count($data_arr) > 5000) {
            $this->setWarning("MSG915", "処理件数を５千件以下にして下さい 処理件数：".get_count($data_arr)."件");
            return false;
        }
        return true;
    }

    //ＣＳＶ出力
    public function getDownloadModel()
    {
        //項目
        $header_err = array("MSGROW"        => "行番号",
                            "MSGREMARK"     => "メッセージ");
        //ヘッダデータ
        $header = array();
        $header["YEAR"]             = "※年度";
        $header["SCHREGNO"]         = "※学籍番号";
        $header["GRADE"]            = "学年";
        $header["HR_CLASS"]         = "クラス";
        $header["ATTENDNO"]         = "出席番号";
        $header["NAME"]             = "生徒氏名";
        $header["DOMI_CD"]          = "※寮コード";
        $header["DOMI_NAME"]        = "寮名";
        $header["DOMI_ENTDAY"]      = "※入寮日";
        $header["DOMI_OUTDAY"]      = "退寮日";

        $header_rei = array();
        $header_rei["YEAR"]             = CTRL_YEAR;
        $header_rei["SCHREGNO"]         = "12345678";
        $header_rei["GRADE"]            = "99";
        $header_rei["HR_CLASS"]         = "999";
        $header_rei["ATTENDNO"]         = "999";
        $header_rei["NAME"]             = "アルプ太郎";
        $header_rei["DOMI_CD"]          = "999";
        $header_rei["DOMI_NAME"]        = "寮名";
        $header_rei["DOMI_ENTDAY"]      = CTRL_DATE;
        $header_rei["DOMI_OUTDAY"]      = (CTRL_YEAR + 1)."-03-31";

        $contents  = "";    //クリア

        $header_data = false;   //出力フラグ
        if ($this->field["OUTPUT"] == "1") {
            //ヘッダ
            $header_data = true;
            $contents .= implode(",", $header)."\n";
            $contents .= implode(",", $header_rei)."\n";
        } elseif ($this->field["OUTPUT"] == "3") {
            //エラー
            $tables = array();
            $tables = array("1" => "SCHREG_DOMITORY_HIST_DAT");
            $dupTable = $this->getDuplicateErrorHeader(PROGRAMID, $tables);
            if (sizeof($dupTable) > 0) {
                $headerErr = $this->duplicateCsvHeader($dupTable, $header);
                $contents .= implode($headerErr, ",")."\n";
            }
            $contents .= implode(",", $header_err)."\n";
        } elseif ($this->field["OUTPUT"] == "4") {
            //データ
            if ($this->field["HEADER"] == "on") {
                $contents .= implode(",", $header)."\n";
            }
        }

        //ヘッダ以外
        if ($this->field["OUTPUT"] != "1") {
            //DB接続
            $db = Query::dbCheckOut();

            if ($this->field["OUTPUT"] == "3") {
                //エラー
                $query = $this->selectCsvErrQuery(PROGRAMID);
            } elseif ($this->field["OUTPUT"] == "4") {
                //データ
                $query = knjx_h211Query::selectMainQuery($this);
            }

            //SQL文発行
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                if ($this->field["OUTPUT"] == "4") {                //データ
                    $row = str_replace(",", ".", $row);     //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
                    $row = str_replace("\r\n", "↓", $row);
                    $row = str_replace("\r", "↓", $row);
                    $row = str_replace("\n", "↓", $row);
                    $array = $header;
                } else {
                    $array = $header_err;
                }

                //ヘッダ順に値をセット
                $comma = "";
                foreach ($array as $key => $headerName) {
                    $contents .= $comma.$row[$key];
                    $comma = ",";
                }
                $contents .= "\n";

                $header_data = true;
            }
            $result->free();
            Query::dbCheckIn($db);
        }

        //ＣＳＶ出力
        if ($header_data) {
            if ($this->field["OUTPUT"] == "3") {        //エラー
                $title = "エラー出力";
            } else {                                //エラー以外
                $title = CTRL_YEAR."年度 "."寮生データ";
            }
            $filename = sprintf("%s.csv", $title);
            common::downloadContents($contents, $filename);
            return true;
        } else {
            $this->warning = "該当データは、ありません。";
            return false;
        }
    }
}
