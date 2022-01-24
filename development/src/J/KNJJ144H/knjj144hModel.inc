<?php

require_once('for_php7.php');

class knjj144hModel extends Model {
    var $data       = array();
    var $cmd;
    var $file;

    function knjj144hModel() {}

    function init() {
        $this->field = array("HEADER"      =>  VARS::post("HEADER"),    //ヘッダの有無
                             "SHORI_MEI"   =>  VARS::post("SHORI_MEI"), //処理名
                             "OUTPUT"      =>  VARS::post("OUTPUT")     //出力選択
                             );

        $this->cmd  = VARS::request("cmd");
        $this->file = VARS::file("FILE");
    }

    function &getExecModel() {

        //ファイルの文字列の取得
        $this->data_arr = array();
        $this->data_arr = $this->GetFileString();

        //データの整合性チェック
        if ( $this->IsDataMatched($this->data_arr) ) {
            $errFlg = $this->checkCsvErrQuery(PROGRAMID);
            if ($errFlg) {
                knjj144hQuery::insertQueryCsv($this);
                $this->setMessage("MSG202", "更新件数".get_count($this->data_arr)."件");
            }
        }
    }

    //新規
    function GetFileString() {
        $temp_arr = array();
        if (!is_uploaded_file($this->file["tmp_name"])) {        //HTTP POSTによりアップロードされたファイルかどうかを調べる
            $this->warning = "ファイル名が不正です。";
        } else {
            $checkdata = array(
                               "SCHREGNO"       => array(8,"学籍番号"),
                               "KAIKIN_FLG"     => array(1,"皆勤フラグ")
                               );

            $db = Query::dbCheckOut();
            $db->autoCommit(false);

            //エラーデータの削除
            $query = $this->deleteQueryErr(PROGRAMID);
            $result = $db->query($query);

            $fp = fopen ($this->file["tmp_name"], "r");
            $i  = 0;
            $line = 1;  //行数
            $header = false;
            $primaryKey1   = array();
            $duplicateCsv1 = array();
            $primaryKey1   = $this->getPrimaryKey('ROU_HATSU_SESSIN_KAI_DAT');
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

                    $data = array("SCHREGNO"   => (strlen(trim($tmp[0]))) ? $tmp[0] : "",
                                  "GRADE"      => (strlen(trim($tmp[1]))) ? $tmp[1] : "",
                                  "HR_CLASS"   => (strlen(trim($tmp[2]))) ? $tmp[2] : "",
                                  "ATTENDNO"   => (strlen(trim($tmp[3]))) ? $tmp[3] : "",
                                  "KAIKIN_FLG" => (strlen(trim($tmp[4]))) ? $tmp[4] : "",
                                  "YEAR"       => CTRL_YEAR
                                  );
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
    function errCheck($db, $data, $data_each, $line, $duplicateCsv1, $primaryKey1) {
        $returnFlg = true;
        $hissuErrFlg     = false;    //必須エラーフラグ
        $lengthErrFlg    = false;    //レングスエラーフラグ
        $errorFlg        = false;    //入力エラーフラグ
        $duplicateCsvErrFlg = false;    //重複エラーフラグ

        $hissuErrMsg     = "";       //必須エラーメッセージ
        $lengthErrMsg    = "";       //レングスエラーメッセージ
        $errorMsg        = "";       //入力エラーメッセージ
        $duplicateCsvErrMsg = "";       //重複エラーメッセージ

        $hissuSep        = "";
        $lengthSep       = "";
        $errorSep        = "";
        $duplicateCsvSep    = "";

        $dupFlg1 = $this->duplicateCsvCheck($duplicateCsv1, $primaryKey1, $data);
        if ($dupFlg1[0] == 1) {
            $returnFlg = $this->errSet($duplicateCsvErrFlg, $duplicateCsvErrMsg, "※重複(CSV):", $duplicateCsvSep, "重複1(". $dupFlg1[1] ."行目)");
        }

        foreach ($data_each as $key => $val) {
            if (in_array($key,array("SCHREGNO"))) {
                //必須項目チェック
                if (!strlen(trim($data[$key]))) {
                    $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[1]);
                }
                //桁数チェック
                if (strlen($data[$key]) != $val[0]) {
                    $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[1]);
                }
            }
            if (in_array($key,array("KAIKIN_FLG"))) {
                //入力チェック
                if ($data[$key] != '0') {
                    if ($data[$key] != '1'){
                        $returnFlg = $this->errSet($errorFlg, $errorMsg, "※入力:", $errorSep, $val[1]);
                    }
                }
            }
        }

        if (!$returnFlg) {
            $checkError = $duplicateCsvErrMsg.$hissuErrMsg.$lengthErrMsg.$errorMsg;
            $checkError = $this->checkErrLength($checkError);
            knjj144hQuery::insertQueryErr($db, $line, $checkError);
        }
        return $returnFlg;
    }

    //データの整合性チェック
    function IsDataMatched(&$data_arr) {
        //５千件を超えたら制限エラー
        if (get_count($data_arr) > 5000) {
            $this->setWarning("MSG915","処理件数を５千件以下にして下さい 処理件数：".get_count($data_arr)."件");
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
        $header = array("SCHREGNO"     => "学籍番号",
                        "GRADE"        => "学年",
                        "HR_CLASS"     => "組",
                        "ATTENDNO"     => "出席番号",
                        "KAIKIN_FLG"   => "皆勤フラグ",
                        "YEAR"         => "年度"
                        );

        $contents  = "";    //クリア

        $header_data = false;   //出力フラグ
        if ($this->field["OUTPUT"]=="2") { //2:エラー出力
            //エラー
            $tables = array();
            $tables = array("1" => "ROU_HATSU_SESSIN_KAI_DAT");
            $dupTable = $this->getDuplicateErrorHeader(PROGRAMID, $tables);
            if (sizeof($dupTable) > 0) {
                $headerErr = $this->duplicateCsvHeader($dupTable, $header);
                $contents .= implode($headerErr, ",")."\n";
            }
            $contents .= implode($header_err, ",")."\n";
        }

        //DB接続
        $db = Query::dbCheckOut();

        //エラー出力
        $query = $this->selectCsvErrQuery(PROGRAMID);

        //SQL文発行
        $result = $db->query($query);
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $contents .= implode($row, ",") ."\n";
            $header_data = true;
        }
        $result->free();
        Query::dbCheckIn($db);

        //ＣＳＶ出力
        if ($header_data) {
            if($this->field["OUTPUT"]=="2"){ //2:エラー出力
                $couse = "エラー出力";
            }
            $filename = sprintf("%s.csv",$couse);
            common::downloadContents($contents, $filename, "SJIS-win");
            return true;
        } else {
            $this->warning = "該当データは、ありません。";
            return false;
        }
    }
}
?>
