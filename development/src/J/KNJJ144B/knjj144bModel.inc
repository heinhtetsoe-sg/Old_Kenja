<?php

require_once('for_php7.php');

class knjj144bModel extends Model {
    var $data       = array();
    var $cmd;
    var $file;

    function knjj144bModel() {}

    function init() {
        $this->field = array("GRADE"       =>  VARS::post("GRADE"),     //学年
                             "HR_CLASS"    =>  VARS::post("HR_CLASS"),  //年組
                             "SEX"         =>  VARS::post("SEX"),       //性別
                             "ATTEND_CD"   =>  VARS::post("ATTEND_CD"), //欠席
                             "HEADER"      =>  VARS::post("HEADER"),    //ヘッダの有無
                             "SHORI_MEI"   =>  VARS::post("SHORI_MEI"), //処理名
                             "OUTPUT"      =>  VARS::post("OUTPUT")     //出力選択
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
            $errFlg = $this->checkCsvErrQuery(PROGRAMID);
            if ($errFlg) {
                if ($this->field["SHORI_MEI"] == "1") {
                    knjj144bQuery::insertQueryCsv($this);
                } else {
                    knjj144bQuery::DeleteQueryCsv($this);
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
            $checkdata = array(
                               "GRADE"          => array(2,"学年"),
                               "HR_CLASS"       => array(3,"組"),
                               "ATTENDNO"       => array(3,"出席番号"),
                               "TIME_H"         => array(2,"タイム(時)"),
                               "TIME_M"         => array(2,"タイム(分)"),
                               "TIME_S"         => array(2,"タイム(秒)"),
                               "REMARK"         => array(60,"備考")
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
            $primaryKey1   = $this->getPrimaryKey('MARATHON_EVENT_DAT');
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
                    $grade       = (strlen(trim($tmp[0]))) ? $tmp[0] : "";
                    $hrClass     = (strlen(trim($tmp[1]))) ? $tmp[1] : "";
                    $attendN0    = (strlen(trim($tmp[2]))) ? $tmp[2] : "";
                    $schregNo    = $db->getOne(knjj144bQuery::getRegd($grade, $hrClass, $attendN0, "schregno"));
                    $schregCnt   = $db->getOne(knjj144bQuery::getRegd($grade, $hrClass, $attendN0, 'cnt'));

                    $data = array("GRADE"      => (strlen(trim($tmp[0]))) ? $tmp[0] : "",
                                  "HR_CLASS"   => (strlen(trim($tmp[1]))) ? $tmp[1] : "",
                                  "ATTENDNO"   => (strlen(trim($tmp[2]))) ? $tmp[2] : "",
                                  "TIME_H"     => (strlen(trim($tmp[3]))) ? $tmp[3] : "",
                                  "TIME_M"     => (strlen(trim($tmp[4]))) ? $tmp[4] : "",
                                  "TIME_S"     => (strlen(trim($tmp[5]))) ? $tmp[5] : "",
                                  "ATTEND_CD"  => (strlen(trim($tmp[6]))) ? $tmp[6] : "",
                                  "REMARK"     => (strlen(trim($tmp[7]))) ? $tmp[7] : "",
                                  "YEAR"       => CTRL_YEAR,
                                  "SEQ"        => '00',
                                  "SCHREGNO"   => $schregNo
                                  );

                    //エラーチェック
                    if ($this->errCheck($db, $data, $checkdata, $line, $duplicateCsv1, $primaryKey1)) {
                        $temp_arr[$i] = $data;
                        $i++;
                    }
                    if ($schregCnt == 1 ) {
                        foreach ($primaryKey1 as $key => $val) {
                            $duplicateCsv1[$line][$val] = $data[$val];
                        }
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
        $existenceErrFlg = false;    //存在エラーフラグ
        $timeErrFlg      = false;    //大小エラーフラグ
        $lengthErrFlg    = false;    //レングスエラーフラグ
        $duplicateCsvErrFlg = false;    //重複エラーフラグ

        $hissuErrMsg     = "";       //必須エラーメッセージ
        $existenceErrMsg = "";       //存在エラーメッセージ
        $timeErrMsg      = "";       //大小エラーメッセージ
        $lengthErrMsg    = "";       //レングスエラーメッセージ
        $duplicateCsvErrMsg = "";       //重複エラーメッセージ

        $hissuSep     = "";
        $existenceSep = "";
        $timeSep      = "";
        $lengthSep    = "";
        $duplicateCsvSep    = "";

        $dupFlg1 = $this->duplicateCsvCheck($duplicateCsv1, $primaryKey1, $data);
        if ($dupFlg1[0] == 1) {
            $returnFlg = $this->errSet($duplicateCsvErrFlg, $duplicateCsvErrMsg, "※重複(CSV):", $duplicateCsvSep, "重複1(". $dupFlg1[1] ."行目)");
        }

        $count = 1;
        foreach ($data_each as $key => $val) {
            //必須項目チェック
            if (in_array($key,array("GRADE","HR_CLASS","ATTENDNO"))) {
                if (!strlen(trim($data[$key]))) {
                    $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[1]);
                }
            }
            //在籍データ存在チェック
            if (in_array($key,array("GRADE","HR_CLASS","ATTENDNO"))) {
                $count++;
                if($count == 3){
                   if ($db->getOne(knjj144bQuery::getRegd($data["GRADE"], $data["HR_CLASS"], $data["ATTENDNO"], 'cnt')) < 1) {
                       $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, "学年/組/出席番号");
                   }
                }
            }
            
            //更新時
            if ($this->field["SHORI_MEI"] == "1") {
                //時間チェック
                if (in_array($key,array("TIME_H"))) {
                    if ($data[$key] >= 24 || $data[$key] < 0) {
                        $returnFlg = $this->errSet($timeErrFlg, $timeErrMsg, "※時間:", $timeSep, $val[1]);
                    }
                }
                if (in_array($key,array("TIME_M","TIME_S"))) {
                    if ($data[$key] >= 60 || $data[$key] < 0) {
                        $returnFlg = $this->errSet($timeErrFlg, $timeErrMsg, "※時間:", $timeSep, $val[1]);
                    }
                }
                //文字数チェック
                if (strlen($data[$key]) > $val[0]) {
                    $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[1]);
                }
            }
        }

        if (!$returnFlg) {
            $checkError = $duplicateCsvErrMsg.$hissuErrMsg.$existenceErrMsg.$timeErrMsg.$lengthErrMsg;
            $checkError = $this->checkErrLength($checkError);
            knjj144bQuery::insertQueryErr($db, $line, $checkError);
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
        $header = array("GRADE"           => "※年",
                        "HR_CLASS"        => "※組",
                        "ATTENDNO"        => "※出席番号",
                        "TIME_H"          => "タイム(時)",
                        "TIME_M"          => "タイム(分)",
                        "TIME_S"          => "タイム(秒)",
                        "ATTEND_CD"       => "出欠情報",
                        "REMARK"          => "備考"
                        );

        $mojisu = array();
        $header_rei = array("GRADE"       => "99",
                            "HR_CLASS"    => "99",
                            "ATTENDNO"    => "999",
                            "TIME_H"      => "99",
                            "TIME_M"      => "99",
                            "TIME_S"      => "99",
                            "ATTEND_CD"   => "1",
                            "REMARK"      => "備考"
                            );

        $contents  = "";    //クリア

        $header_data = false;   //出力フラグ
        if ($this->field["OUTPUT"]=="1") { //1:ヘッダ出力
            //ヘッダ
            $header_data = true;
            $contents .= implode($header, ",")."\n";
            $contents .= implode($header_rei, ",")."\n";
        } else if ($this->field["OUTPUT"]=="3") { //3:エラー出力
            //エラー
            $headerDuplicate = array("YEAR"       => "※年度",
                                     "SEQ"        => "※連番",
                                     "SCHREGNO"   => "※学籍番号"
                                    );
            $tables = array();
            $tables = array("1" => "MARATHON_EVENT_DAT");
            $dupTable = $this->getDuplicateErrorHeader(PROGRAMID, $tables);
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
                $query = $this->selectCsvErrQuery(PROGRAMID);
            } else if ($this->field["OUTPUT"]=="4") { //4:データ出力
                //データ
                $query = knjj144bQuery::selectMainQuery($this);
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
                $couse = "マラソン大会データ";
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
