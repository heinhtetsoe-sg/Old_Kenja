<?php

require_once('for_php7.php');

class knjx651csvModel extends Model {
    var $field      = array();
    var $data       = array();
    var $cmd;
    var $file;
    var $subclasscd;
    var $control;           //コントロールマスタ
    var $staffcd;           //登録者コード

    function knjx651csvModel() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd = STAFFCD; //登録者コード
    }

    function init() {
        $this->subclasscd = '999999';

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
            if ($this->field["SHORI_MEI"] == "1") {
                $cnt = knjx651csvQuery::insertQueryCsv($this,$this->data_arr);
            } else {
                $cnt = knjx651csvQuery::DeleteQueryCsv($this,$this->data_arr);
            }
            $db = Query::dbCheckOut();
            $errcnt = $db->getOne("SELECT COUNT(*) FROM W_CSVMSG_DAT");
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
            $checkdata = array("YEAR"       => array(4,"年度"),
                               "SEMESTER"   => array(1,"学期"),
                               "SCHREGNO"   => array(8,"学籍番号"),
                               "TEST_CD"    => array(4,"テストコード"),
                               "REMARK"     => array(90,"備考")
                               );

            $db = Query::dbCheckOut();
            $db->autoCommit(false);

            //エラーデータの削除
            $query = knjx651csvQuery::deleteQueryErr();
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

                    $data = array("YEAR"             => (strlen(trim($tmp[4]))) ? sprintf("%04d",$tmp[4]) : "",
                                  "SEMESTER"         => (strlen(trim($tmp[5]))) ? sprintf("%01d",$tmp[5]) : "",
                                  "SCHREGNO"         => (strlen(trim($tmp[6]))) ? sprintf("%08d",$tmp[6]) : "",
                                  "TEST_CD"          => (strlen(trim($tmp[7]))) ? sprintf("%04d",$tmp[7]) : "",
                                  "REMARK"           => (strlen(trim($tmp[8]))) ? $tmp[8] : ""
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
    function errCheck($db, $data, $data_each, $line)
    {
        $returnFlg = true;
        $hissuErrFlg     = false;    //必須エラーフラグ
        $numericErrFlg   = false;    //数字エラーフラグ
        $lengthErrFlg    = false;    //レングスエラーフラグ

        $hissuErrMsg     = "";       //必須エラーメッセージ
        $numericErrMsg   = "";       //数字エラーメッセージ
        $lengthErrMsg    = "";       //レングスエラーメッセージ

        $hissuSep     = "";
        $existenceSep = "";
        $numericSep   = "";
        $lengthSep    = "";

        foreach ($data_each as $key => $val) {

            //必須項目チェック
            if (in_array($key,array("YEAR","SEMESTER","SCHREGNO", "TEST_CD"))) {
                if (!strlen(trim($data[$key]))) {
                    $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[1]);
                }
            }

            //数値チェック
            if (in_array($key,array("YEAR","SEMESTER","SCHREGNO", "TEST_CD"))) {
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
            $check_error = $hissuErrMsg.$numericErrMsg.$lengthErrMsg;
            if (strlen($check_error) > 120) {
                $check_error = mb_strcut($check_error,0,115);
                $check_error .= "..他";
            }
            knjx651csvQuery::insertQueryErr($db, $line, $check_error);
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
        $header = array("GRADE"         => "学年",
                        "HR_CLASS"      => "クラス",
                        "ATTENDNO"      => "出席番号",
                        "NAME"          => "氏名",
                        "YEAR"          => "※年度",
                        "SEMESTER"      => "※学期",
                        "SCHREGNO"      => "※学籍番号",
                        "TEST_CD"       => "※テストコード",
                        "REMARK"        => "※備考"
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
                            "REMARK"           => "全角30文字"
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
                $query = knjx651csvQuery::selectCsvErrQuery();
            } else if ($this->field["OUTPUT"]=="4") { //4:データ出力
                //データ
                $query = knjx651csvQuery::selectMainQuery($this);
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
                $couse = "成績一覧表の備考データ";
            }
            $filename = sprintf("%s.csv",$couse);
            $filename = i18n_convert($filename,"SJIS-win");
            $contents = i18n_convert($contents,"SJIS-win");
            /* HTTPヘッダの出力 */
            i18n_http_output("pass");
            header("Accept-Ranges: none");
            header("Content-Disposition: inline; filename=$filename");
            header("Content-Transfer-Encoding: binary");
            header("Content-Length: ". strlen($contents) );
            header("Content-Type: text/octet-stream");

            echo $contents;
            return true;
        } else {
            $this->warning = "該当データは、ありません。";
            return false;
        }
    }
}
//var_dump($_REQUEST);
?>
