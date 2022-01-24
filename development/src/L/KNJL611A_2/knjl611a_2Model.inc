<?php

require_once('for_php7.php');

class knjl611a_2Model extends Model {
    var $field      = array();
    var $data       = array();
    var $cmd;
    var $file;
    var $year;              //入試年度
    var $semester;          //現在処理学期
    var $examno;            //受験番号
    var $applicantdiv;      //入試制度
    var $sendPrgId;
    var $sendAuth;
    var $auth;

    function knjl611a_2Model() {
    }

    function init() {
        //起動時に初期化
        if(VARS::get("PROGRAMID")) {
            unset($this->examno);
            unset($this->year);
            unset($this->getPrgId);
            unset($this->sendAuth);
            unset($this->auth);
        }
        if(VARS::get("SEND_PRGID")){
            $this->getPrgId     = VARS::get("SEND_PRGID");
            $this->sendAuth     = VARS::get("SEND_AUTH");
            $this->applicantdiv = VARS::get("SEND_APPLICANTDIV");
            $this->testdiv      = VARS::get("SEND_TESTDIV");
            $this->examno       = VARS::get("SEND_EXAMNO");
        }
        $this->auth = $this->getPrgId ? $this->sendAuth : AUTHORITY;
        
        $this->cmd = VARS::request("cmd");
        $this->year = CTRL_YEAR + 1;

        $this->field = array("OUTPUT"           =>  VARS::post("OUTPUT"),   //出力取込種別 1:ヘッダ出力 2:データ取込 3:エラー出力 4:データ出力
                             "HEADER"           =>  VARS::post("HEADER")    //ヘッダ有
                             );

        $this->file = VARS::file("FILE");
    }

    function &getExecModel() {
        //ファイルの文字列の取得
        $this->data_arr = array();
        $this->data_arr = $this->GetFileString();

        //データの整合性チェック
        if ( $this->IsDataMatched($this->data_arr) ) {

            $cnt = knjl611a_2Query::insertQueryCsv($this,$this->data_arr);

            $db = Query::dbCheckOut();
            $query  = " SELECT COUNT(*) FROM W_CSVMSG_PRG_DAT WHERE PROGRAMID = '".PROGRAMID."' ";
            $errcnt = $db->getOne($query);
            Query::dbCheckIn($db);

            $this->setMessage("MSG202","処理済件数　：".$cnt."件\\nエラー件数    ：".$errcnt."件");
        }
    }

    //取込
    function GetFileString() {
        $temp_arr = array();
        if (!is_uploaded_file($this->file["tmp_name"])) {        //HTTP POSTによりアップロードされたファイルかどうかを調べる
            $this->warning = "ファイル名が不正です。";
        } else {
            $checkdata = array("ENTEXAMYEAR"      => array(4, "年度"),
                               "EXAMNO"           => array(4, "※受験番号"),
                               "TOTAL5"           => array(2, "5科評定合計"),
                               "ABSENCE_DAYS3"    => array(1, "欠席日数"),
                               "SPECIALACTREC"    => array(2, "生徒会活動"),
                               "TOTALSTUDYTIME"   => array(1, "特別活動推薦")
                               );

            $db = Query::dbCheckOut();
            $db->autoCommit(false);

            //エラーデータの削除
            $query = knjl611a_2Query::deleteQueryErr();
            $result = $db->query($query);

            $fp = fopen ($this->file["tmp_name"], "r");
            $i  = 0;
            $line = 1;  //行数
            $header = false;
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

                    $dataCnt = 0;
                    $data = array("ENTEXAMYEAR"     => $tmp[$dataCnt++],
                                  "EXAMNO"          => $tmp[$dataCnt++],
                                  "TOTAL5"          => $tmp[$dataCnt++],
                                  "ABSENCE_DAYS3"   => $tmp[$dataCnt++],
                                  "SPECIALACTREC"   => $tmp[$dataCnt++],
                                  "TOTALSTUDYTIME"  => $tmp[$dataCnt++]
                                  );

                    //エラーチェック
                    if ($this->errCheck($db, $data, $checkdata, $line)) {
                        $temp_arr[$i] = $data;
                        $i++;
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
    function errCheck($db, $data, $data_each, $line) {
        $returnFlg = true;
        $hissuErrFlg     = false;    //必須エラーフラグ
        $existenceErrFlg = false;    //存在エラーフラグ
        $numericErrFlg   = false;    //数字エラーフラグ
        $lengthErrFlg    = false;    //レングスエラーフラグ

        $hissuErrMsg     = "";       //必須エラーメッセージ
        $existenceErrMsg = "";       //存在エラーメッセージ
        $numericErrMsg   = "";       //数字エラーメッセージ
        $lengthErrMsg    = "";       //レングスエラーメッセージ

        $hissuSep     = "";
        $existenceSep = "";
        $numericSep   = "";
        $lengthSep    = "";

        foreach ($data_each as $key => $val) {
        
            //必須項目チェック
            if (in_array($key,array("ENTEXAMYEAR", "EXAMNO"))) {
                if (!strlen(trim($data[$key]))) {
                    $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[1]);
                }
            }

            //数値チェック
            if ($data[$key] != "" && !is_numeric($data[$key])) {
                $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, $val[1]);
            }

            //文字数チェック
            if (strlen($data[$key]) > $val[0]) {
                $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[1]);
            }

            //年度チェック
            if (in_array($key,array("ENTEXAMYEAR"))) {
                if ($data[$key] != $this->year) {
                    $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※年度外", $hissuSep, "");
                }
            }

        }

        if (!$returnFlg) {
            $check_error = $hissuErrMsg.$existenceErrMsg.$numericErrMsg.$lengthErrMsg;
            if (strlen($check_error) > 120) {
                $check_error = mb_strcut($check_error,0,115);
                $check_error .= "..他";
            }
            knjl611a_2Query::insertQueryErr($db, $line, $check_error);
        }
        return $returnFlg;
    }

    //エラーメッセージをセット
    function errSet(&$errFlg, &$errMsg, $firstMsg, &$sep, $val) {
        if (!$errFlg) {
            $errMsg .= $firstMsg;
            $errFlg = true;
        }
        $errMsg .= $sep.$val;
        $sep = "/";
        return false;
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
        $header = array("ENTEXAMYEAR"       => "※年度",
                        "EXAMNO"            => "※受験番号",
                        "TOTAL5"            => "5科評定合計",
                        "ABSENCE_DAYS3"     => "欠席日数",
                        "SPECIALACTREC"     => "生徒会活動",
                        "TOTALSTUDYTIME"    => "特別活動推薦"
                        );

        $mojisu = array();
        $header_rei = array("ENTEXAMYEAR"       => "9999",
                            "EXAMNO"            => "9999",
                            "TOTAL5"            => "99",
                            "ABSENCE_DAYS3"     => "9",
                            "SPECIALACTREC"     => "99",
                            "TOTALSTUDYTIME"    => "9"
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
                $query = knjl611a_2Query::selectCsvErrQuery();
            } else if ($this->field["OUTPUT"]=="4") { //4:データ出力
                //データ
                $query = knjl611a_2Query::selectMainQuery($this);
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
                $couse = $this->year."年度内申データ";
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
//var_dump($_REQUEST);
?>
