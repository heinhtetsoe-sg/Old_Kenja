<?php

require_once('for_php7.php');

class knje389Model extends Model {
    var $field      = array();
    var $data       = array();
    var $cmd;
    var $file;
    var $control;           //コントロールマスタ
    var $staffcd;           //登録者コード
    var $sendPrgId;
    var $sendAuth;
    var $auth;

    function knje389Model() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd = STAFFCD; //登録者コード
    }

    function init() {
        if (VARS::get("PROGRAMID")) {
            unset($this->sendPrgId);
        }
        if (VARS::get("SEND_PRGID")) {
            $this->sendPrgId = VARS::get("SEND_PRGID");
            // $this->sendAuth = VARS::get("SEND_AUTH");
        }
        //権限
        // $this->auth = $this->sendPrgId ? $this->sendAuth : AUTHORITY;

        $this->field = array("OUTPUT"           =>  VARS::post("OUTPUT"),           //出力取込種別 1:ヘッダ出力 2:データ取込 3:エラー出力 4:データ出力
                             "YEAR_SEMESTER"    =>  VARS::post("YEAR_SEMESTER"),    //年度&学期
                             "GRADE"            =>  VARS::post("GRADE"),            //学年
                             "SORT"             =>  VARS::post("SORT"),             //出力順
                             "HEADER"           =>  VARS::post("HEADER"),           //ヘッダ有
                             "SHORI_MEI"        =>  VARS::post("SHORI_MEI")         //処理名
                             );

        $this->cmd = VARS::request("cmd");
        $this->file = VARS::file("FILE");
    }

    function &getExecModel()
    {
        //ファイルの文字列の取得
        $this->data_arr = array();
        $this->data_arr = $this->GetFileString();

        //データの整合性チェック
        if ($this->IsDataMatched($this->data_arr)) {
            $errFlg = $this->checkCsvErrQuery(PROGRAMID);
            if ($errFlg) {
                if ($this->field["SHORI_MEI"] == "1") {
                    knje389Query::insertQueryCsv($this, $this->data_arr);
                } else {
                    knje389Query::DeleteQueryCsv($this, $this->data_arr);
                }
                $this->setMessage("MSG202", "更新件数".get_count($this->data_arr)."件");
            }
        }
    }

    //取込
    function GetFileString()
    {
        $temp_arr = array();
        if (!is_uploaded_file($this->file["tmp_name"])) {        //HTTP POSTによりアップロードされたファイルかどうかを調べる
            $this->warning = "ファイル名が不正です。";
        } else {
            $checkdata = array("YEAR"               => array(4,"年度"),
                               "SEMESTER"           => array(1,"学期"),
                               "NAME"               => array(150,"氏名"),
                               "SCHREGNO"           => array(8,"学籍番号"),
                               "TOTAL_SCORE"        => array(5,"総学力点"),
                               "CLASS_SCORE"        => array(4,"教科点計"),
                               "ABILITY_SCORE"      => array(4,"学力点計"),
                               "TOEFL_SCORE"        => array(4,"TOEFL-ITP計"),
                               "QUALIFIED_SCORE"    => array(4,"資格点計"),
                               "ADJUSTMENT_SCORE"   => array(4,"加減点計")
                               );

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
            $primaryKey2 = array();
            $duplicateCsv1 = array();
            $duplicateCsv2 = array();
            $primaryKey1 = $this->getPrimaryKey('AFT_TOTAL_STUDY_BATCH_DAT');
            $primaryKey2 = $this->getPrimaryKey('AFT_TOTAL_STUDY_ADJUSTMENT_DAT');
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

                    $data = array("YEAR"                => (strlen(trim($tmp[0]))) ? sprintf("%04d", $tmp[0]) : "",
                                  "SEMESTER"            => (strlen(trim($tmp[1]))) ? $tmp[1] : "",
                                  "NAME"                => (strlen(trim($tmp[5]))) ? $tmp[5] : "",
                                  "SCHREGNO"            => (strlen(trim($tmp[6]))) ? sprintf("%08d", $tmp[6]) : "",
                                  "TOTAL_SCORE"         => (strlen(trim($tmp[7]))) ? $tmp[7] : "",
                                  "CLASS_SCORE"         => (strlen(trim($tmp[8]))) ? $tmp[8] : "",
                                  "ABILITY_SCORE"       => (strlen(trim($tmp[9]))) ? $tmp[9] : "",
                                  "TOEFL_SCORE"         => (strlen(trim($tmp[10]))) ? $tmp[10] : "",
                                  "QUALIFIED_SCORE"     => (strlen(trim($tmp[11]))) ? $tmp[11] : "",
                                  "ADJUSTMENT_SCORE"    => (strlen(trim($tmp[12]))) ? $tmp[12] : ""
                                  );

                    //エラーチェック
                    if ($this->errCheck($db, $data, $checkdata, $line, $duplicateCsv1, $duplicateCsv2, $primaryKey1, $primaryKey2)) {
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

    //エラーチェック
    function errCheck($db, $data, $data_each, $line, $duplicateCsv1, $duplicateCsv2, $primaryKey1, $primaryKey2) {
        $returnFlg = true;
        $hissuErrFlg        = false;    //必須エラーフラグ
        $numericErrFlg      = false;    //数字エラーフラグ
        $lengthErrFlg       = false;    //レングスエラーフラグ
        $duplicateCsvErrFlg = false;    //重複エラーフラグ

        $hissuErrMsg        = "";       //必須エラーメッセージ
        $numericErrMsg      = "";       //数字エラーメッセージ
        $lengthErrMsg       = "";       //レングスエラーメッセージ
        $duplicateCsvErrMsg = "";       //重複エラーメッセージ

        $hissuSep           = "";
        $numericSep         = "";
        $lengthSep          = "";
        $duplicateCsvSep    = "";

        if ($this->field["SHORI_MEI"] == "1") {
            $dupFlg1 = $this->duplicateCsvCheck($duplicateCsv1, $primaryKey1, $data);
            if ($dupFlg1[0] == 1) {
                $returnFlg = $this->errSet($duplicateCsvErrFlg, $duplicateCsvErrMsg, "※重複(CSV):", $duplicateCsvSep, "重複1(". $dupFlg1[1] ."行目)");
            }
            $dupFlg2 = $this->duplicateCsvCheck($duplicateCsv2, $primaryKey2, $data);
            if ($dupFlg2[0] == 1) {
                $returnFlg = $this->errSet($duplicateCsvErrFlg, $duplicateCsvErrMsg, "※重複(CSV):", $duplicateCsvSep, "重複2(". $dupFlg2[1] ."行目)");
            }
        } else {
            $dupFlg1 = $this->duplicateCsvCheck($duplicateCsv1, $primaryKey1, $data);
            if ($dupFlg1[0] == 1) {
                $returnFlg = $this->errSet($duplicateCsvErrFlg, $duplicateCsvErrMsg, "※重複(CSV):", $duplicateCsvSep, "重複1(". $dupFlg1[1] ."行目)");
            }
        }

        foreach ($data_each as $key => $val) {
            //必須項目チェック
            if (in_array($key, array("YEAR", "SEMESTER", "SCHREGNO"))) {
                if (!strlen(trim($data[$key]))) {
                    $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[1]);
                }
            }

            //数値チェック
            if (in_array($key, array("YEAR", "SEMESTER", "ADJUSTMENT_SCORE"))) {
                if ($data[$key] != "" && !is_numeric($data[$key])) {
                    $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, $val[1]);
                }
            }

            if ($this->field["SHORI_MEI"] == "1") {
                //文字数チェック
                if (in_array($key, array("YEAR", "SCHREGNO", "ADJUSTMENT_SCORE"))) {
                    if (strlen($data[$key]) > $val[0]) {
                        $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[1]);
                    }
                }
            }

            //年度チェック
            if (in_array($key, array("YEAR"))) {
                if ((trim($data[$key]) < CTRL_YEAR)) {
                    $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※過年度", $hissuSep, $val[1]);
                }
                if ((trim($data[$key]) > CTRL_YEAR)) {
                    $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※未来年度", $hissuSep, $val[1]);
                }
            }
        }

        if (!$returnFlg) {
            $checkError = $duplicateCsvErrMsg.$hissuErrMsg.$numericErrMsg.$lengthErrMsg;
            $checkError = $this->checkErrLength($checkError);
            knje389Query::insertQueryErr($db, $line, $checkError);
        }
        return $returnFlg;
    }

    //データの整合性チェック
    function IsDataMatched(&$data_arr)
    {
        //５千件を超えたら制限エラー
        if (get_count($data_arr) > 5000) {
            $this->setWarning("MSG915", "処理件数を５千件以下にして下さい 処理件数：".get_count($data_arr)."件");
            return false;
        }
        return true;
    }

    //ＣＳＶ出力
    function getDownloadModel() {
        //項目
        $header_err = array("MSGROW"    => "行番号",
                            "MSGREMARK" => "メッセージ");
        //ヘッダデータ
        $header = array("YEAR"              => "※年度",
                        "SEMESTER"          => "※学期",
                        "GRADE"             => "学年",
                        "HR_CLASS"          => "クラス",
                        "ATTENDNO"          => "出席番号",
                        "NAME"              => "氏名",
                        "SCHREGNO"          => "※学籍番号",
                        "TOTAL_SCORE"       => "総学力点",
                        "CLASS_SCORE"       => "教科点計",
                        "ABILITY_SCORE"     => "学力点計",
                        "TOEFL_SCORE"       => "TOELF-ITP計",
                        "QUALIFIED_SCORE"   => "資格点計",
                        "ADJUSTMENT_SCORE"  => "加減点計"
                        );

        $mojisu = array();
        $header_rei = array("YEAR"              => "9999",
                            "SEMESTER"          => "1",
                            "GRADE"             => "99",
                            "HR_CLASS"          => "999",
                            "ATTENDNO"          => "999",
                            "NAME"              => "アルプ太郎",
                            "SCHREGNO"          => "12345678",
                            "TOTAL_SCORE"       => "12345",
                            "CLASS_SCORE"       => "1234",
                            "ABILITY_SCORE"     => "1234",
                            "TOEFL_SCORE"       => "1234",
                            "QUALIFIED_SCORE"   => "1234",
                            "ADJUSTMENT_SCORE"  => "1234"
                            );

        $contents  = "";    //クリア

        $header_data = false;   //出力フラグ
        if ($this->field["OUTPUT"]=="1") { //1:ヘッダ出力
            //ヘッダ
            $header_data = true;
            $contents .= implode($header, ",")."\n";
            $contents .= implode($header_rei, ",")."\n";
        } elseif ($this->field["OUTPUT"] == "3") { //3:エラー出力
            $tables = array();
            if ($this->field["SHORI_MEI"] == "1") {
                $tables = array("1" => "AFT_TOTAL_STUDY_BATCH_DAT", "2" => "AFT_TOTAL_STUDY_ADJUSTMENT_DAT");
            } else {
                $tables = array("1" => "AFT_TOTAL_STUDY_BATCH_DAT");
            }
            $dupTable = $this->getDuplicateErrorHeader(PROGRAMID, $tables);
            if (sizeof($dupTable) > 0) {
                $headerErr = $this->duplicateCsvHeader($dupTable, $header);
                $contents .= implode($headerErr, ",")."\n";
            }
            //エラー
            $contents .= implode($header_err, ",")."\n";
        } else if ($this->field["OUTPUT"]=="4") { //4:データ出力
            //データ
            if( $this->field["HEADER"]=="on" ) $contents .= implode($header, ",")."\n";
        }

        //ヘッダ以外
        if ($this->field["OUTPUT"] != "1") { //1:ヘッダ出力
            //DB接続
            $db = Query::dbCheckOut();

            if ($this->field["OUTPUT"] == "3") { //3:エラー出力
                //エラー
                $query = $this->selectCsvErrQuery(PROGRAMID);
            } else if ($this->field["OUTPUT"]=="4") { //4:データ出力
                //データ
                $query = knje389Query::selectMainQuery($this);
            }

            //SQL文発行
            $result = $db->query($query);
            while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                if($this->field["OUTPUT"]=="4"){ //4:データ出力
                    $row = str_replace("\r\n","",str_replace(",",".",$row));   //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
                }
                $contents .= implode($row, ",") ."\n";
                $header_data = true;
            }
            $result->free();
            Query::dbCheckIn($db);
        }

        //ＣＳＶ出力
        if ($header_data) {
            if($this->field["OUTPUT"]=="3"){ //3:エラー出力
                $couse = "エラー出力";
            } else {                         //エラー以外
                $couse = "総学力の加減点";
            }
            $filename = sprintf("%s.csv",$couse);
            common::downloadContents($contents, $filename, "SJIS-win");
            return true;
        } else {
            $this->setWarning("MSG303");
            return false;
        }
    }
}
?>
