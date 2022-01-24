<?php

require_once('for_php7.php');

class knjx655ccsvModel extends Model {
    var $field      = array();
    var $data       = array();
    var $cmd;
    var $file;
    var $control;           //コントロールマスタ
    var $staffcd;           //登録者コード

    function knjx655ccsvModel() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd = STAFFCD; //登録者コード
    }

    function init() {
        $this->field = array("OUTPUT"           =>  VARS::post("OUTPUT"),         //出力取込種別 1:ヘッダ出力 2:データ取込 3:エラー出力 4:データ出力
                             "SEMESTER"         =>  VARS::post("SEMESTER"),       //学期
                             "TEST_CD"          =>  VARS::post("TEST_CD"),        //テストコード
                             "GRADE_HR_CLASS"   =>  VARS::post("GRADE_HR_CLASS"), //年組一覧
                             "HEADER"           =>  VARS::post("HEADER"),         //ヘッダ有
                             "SHORI_MEI"        =>  VARS::post("SHORI_MEI")       //処理名
                             );

        $this->cmd = VARS::request("cmd");
        $this->file = VARS::file("FILE");
    }

    function &getExecModel() {
        //ファイルの文字列の取得
        $this->data_arr = array();
        $this->data_arr = $this->GetFileString();

        //データの整合性チェック
        if ( $this->IsDataMatched($this->data_arr) ) {
            $errFlg = $this->checkCsvErrQuery("KNJX_D655C");
            if ($errFlg) {
                if ($this->field["SHORI_MEI"] == "1") {
                    knjx655ccsvQuery::insertQueryCsv($this, $this->data_arr);
                } else {
                    knjx655ccsvQuery::DeleteQueryCsv($this, $this->data_arr);
                }
                $this->setMessage("MSG202", "更新件数".get_count($this->data_arr)."件");
            }
        }
    }

    //取込
    function GetFileString() {
        $temp_arr = array();
        if (!is_uploaded_file($this->file["tmp_name"])) {        //HTTP POSTによりアップロードされたファイルかどうかを調べる
            $this->warning = "ファイル名が不正です。";
        } else {
            $checkdata = array("YEAR"       => array(4,"年度"),
                               "SEMESTER"   => array(1,"学期"),
                               "SCHREGNO"   => array(8,"学籍番号"),
                               "TEST_CD"    => array(4,"テストコード"),
                               "REMARK1"    => array(54,"成績を完備しない生徒")
                               );

            $db = Query::dbCheckOut();
            $db->autoCommit(false);

            //エラーデータの削除
            $query = $this->deleteQueryErr("KNJX_D655C");
            $result = $db->query($query);

            $fp = fopen ($this->file["tmp_name"], "r");
            $i  = 0;
            $line = 1;  //行数
            $header = false;
            $primaryKey1   = array();
            $duplicateCsv1 = array();
            $primaryKey1   = $this->getPrimaryKey("HEXAM_RECORD_REMARK_DAT");
            while (!feof ($fp)) {
                $buffer = fgets($fp, 4096);
                if (trim($buffer) != '') {
                    if (!$header && $this->field["HEADER"] == "on"){
                        $header = true;
                        $line++;
                        continue;
                    }
                    //文字コードを判別、改行コードを削除
                    $buffer = trim(i18n_convert($buffer, i18n_http_output(), "SJIS-win"));
                    $tmp    = common::csv2array($buffer);

                    $data = array("YEAR"             => (strlen(trim($tmp[4]))) ? sprintf("%04d",$tmp[4]) : "",
                                  "SEMESTER"         => (strlen(trim($tmp[5]))) ? sprintf("%01d",$tmp[5]) : "",
                                  "SCHREGNO"         => (strlen(trim($tmp[6]))) ? sprintf("%08d",$tmp[6]) : "",
                                  "TEST_CD"          => (strlen(trim($tmp[7]))) ? sprintf("%04d",$tmp[7]) : "",
                                  "REMARK1"          => (strlen(trim($tmp[8]))) ? $tmp[8] : ""
                                  );
                    $testkindcd = substr($data["TEST_CD"], 0, 2);
                    $testitemcd = substr($data["TEST_CD"], -2);
                    $data["TESTKINDCD"] = $testkindcd;
                    $data["TESTITEMCD"] = $testitemcd;
                    $data["REMARK_DIV"] = "5";

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
    function errCheck($db, $data, $data_each, $line, $duplicateCsv1, $primaryKey1)
    {
        $returnFlg     = true;
        $hissuErrFlg   = false;    //必須エラーフラグ
        $numericErrFlg = false;    //数字エラーフラグ
        $lengthErrFlg  = false;    //レングスエラーフラグ
        $duplicateCsvErrFlg = false;

        $hissuErrMsg   = "";       //必須エラーメッセージ
        $numericErrMsg = "";       //数字エラーメッセージ
        $lengthErrMsg  = "";       //レングスエラーメッセージ
        $duplicateCsvErrMsg = "";

        $hissuSep     = "";
        $numericSep   = "";
        $lengthSep    = "";
        $duplicateCsvSep = "";

        $dupFlg1 = $this->duplicateCsvCheck($duplicateCsv1, $primaryKey1, $data);
        if ($dupFlg1[0] == 1) {
            $returnFlg = $this->errSet($duplicateCsvErrFlg, $duplicateCsvErrMsg, "※重複(CSV):", $duplicateCsvSep, "重複1(". $dupFlg1[1] ."行目)");
        }

        foreach ($data_each as $key => $val) {

            //必須項目チェック
            if (in_array($key, array("YEAR", "SEMESTER", "SCHREGNO", "TEST_CD"))) {
                if (!strlen(trim($data[$key]))) {
                    $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[1]);
                }
            }

            //数値チェック
            if (in_array($key, array("YEAR", "SEMESTER", "SCHREGNO", "TEST_CD"))) {
                if ($data[$key] != "" && !is_numeric($data[$key])) {
                    $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, $val[1]);
                }
            }

            if ($this->field["SHORI_MEI"] == "1") {
                //文字数チェック
                if (strlen($data[$key]) > $val[0]) {
                    $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[1]);
                }
            }
        }

        if (!$returnFlg) {
            $checkError = $duplicateCsvErrMsg.$hissuErrMsg.$numericErrMsg.$lengthErrMsg;
            $checkError = $this->checkErrLength($checkError);
            knjx655ccsvQuery::insertQueryErr($db, $line, $checkError);
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
        $header = array("GRADE"         => "学年",
                        "HR_CLASS"      => "クラス",
                        "ATTENDNO"      => "出席番号",
                        "NAME"          => "氏名",
                        "YEAR"          => "※年度",
                        "SEMESTER"      => "※学期",
                        "SCHREGNO"      => "※学籍番号",
                        "TEST_CD"       => "※テストコード",
                        "REMARK1"       => "※成績を完備しない生徒"
                        );
        $header["DUMMY"] = "DUMMY";

        $mojisu = array();
        $header_rei = array("GRADE"            => "99",
                            "HR_CLASS"         => "999",
                            "ATTENDNO"         => "999",
                            "NAME"             => "アルプ太郎",
                            "YEAR"             => "9999",
                            "SEMESTER"         => "9",
                            "SCHREGNO"         => "12345678",
                            "TEST_CD"          => "1234",
                            "REMARK1"          => "全角15文字"
                            );

        $header_rei["DUMMY"] = "DUMMY";

        $contents  = "";    //クリア

        $header_data = false;   //出力フラグ
        if ($this->field["OUTPUT"]=="1") { //1:ヘッダ出力
            //ヘッダ
            $header_data = true;
            $contents .= implode($header, ",")."\n";
            $contents .= implode($header_rei, ",")."\n";
        } else if ($this->field["OUTPUT"]=="3") { //3:エラー出力
            //エラー
            $headerDuplicate = array("YEAR"       => "年度",
                                     "SEMESTER"   => "学期",
                                     "TESTKINDCD" => "テスト種別",
                                     "TESTITEMCD" => "テスト項目",
                                     "SCHREGNO"   => "学籍番号",
                                     "REMARK_DIV" => "成績区分");
            $tables = array();
            $tables = array("1" => "HEXAM_RECORD_REMARK_DAT");
            $dupTable = $this->getDuplicateErrorHeader("KNJX_D655C", $tables);
            if (sizeof($dupTable) > 0) {
                $headerErr = $this->duplicateCsvHeader($dupTable, $headerDuplicate);
                $contents .= implode($headerErr, ",")."\n";
            }
            $contents .= implode($header_err, ",")."\n";
        } else if ($this->field["OUTPUT"]=="4") { //4:データ出力
            //データ
            if( $this->field["HEADER"]=="on" ) $contents .= implode($header, ",")."\n";
        }

        //ヘッダ以外
        if ($this->field["OUTPUT"] != "1") { //1:ヘッダ出力
            //DB接続
            $db = Query::dbCheckOut();

            if($this->field["OUTPUT"]=="3") { //3:エラー出力
                //エラー
                $query = $this->selectCsvErrQuery("KNJX_D655C");
            } else if ($this->field["OUTPUT"]=="4") { //4:データ出力
                //データ
                $query = knjx655ccsvQuery::selectMainQuery($this);
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
                $couse = "学業成績総括表個人所見データ";
            }
            $filename = sprintf("%s.csv",$couse);
            common::downloadContents($contents, $filename);
            return true;
        } else {
            $this->warning = "該当データは、ありません。";
            return false;
        }
    }
}
?>
