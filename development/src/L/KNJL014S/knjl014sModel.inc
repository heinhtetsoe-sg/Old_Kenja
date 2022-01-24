<?php

require_once('for_php7.php');

class knjl014sModel extends Model {
    var $field      = array();
    var $data       = array();
    var $cmd;
    var $file;
    var $control; //コントロールマスタ
    var $staffcd; //登録者コード
    var $fieldSize = array();
    var $test_year;

    function init() {
        common::GetControlMaster_Fnc($this->control);

        $this->field = array("OUTPUT"        =>  VARS::post("OUTPUT"),        //出力取込種別
                             "APPLICANTDIV"  =>  VARS::post("APPLICANTDIV"),  //入試制度
                             "SUPPLEMENT"    =>  VARS::post("SUPPLEMENT"),    //追検査者のみ出力する
                             "SYUTURYOKUJUN" =>  VARS::post("SYUTURYOKUJUN"), //出力順
                             "HEADER"        =>  VARS::post("HEADER"),        //ヘッダ有
                             "SHORI_MEI"     =>  VARS::post("SHORI_MEI")      //処理名
                             );

        //最終学期を取得
        $rtnSem = knjl014sQuery::getSemesterLast();
        $this->test_year = ($rtnSem == CTRL_SEMESTER) ? CTRL_YEAR + 1 : CTRL_YEAR;
        $this->cmd = VARS::request("cmd");

        if (VARS::file("FILE")){
            $this->field["FILE"] = VARS::file("FILE");        //ファイルのアップロード
            $this->field["FILE"]["name"] = mb_convert_encoding($this->field["FILE"]["name"], "SJIS-win", "UTF-8");
        }
    }

    /***************/
    /* CSVの取込み */
    /***************/
    function &getExecModel() {
        //ファイルの文字列の取得
        $this->data_arr = array();
        $this->data_arr = $this->GetFileString();

        //データの整合性チェック
        if ( $this->IsDataMatched($this->data_arr) ) {
            if ($this->field["SHORI_MEI"] == "1") { //1:更新 2:削除
                $cnt = knjl014sQuery::udpateQueryCsv($this,$this->data_arr);
            } else {
//                $cnt = knjl014sQuery::DeleteQueryCsv($this,$this->data_arr);
            }
            $db = Query::dbCheckOut();
            $errcnt = $db->getOne("SELECT COUNT(*) FROM W_CSVMSG_DAT");
            Query::dbCheckIn($db);

            $this->setMessage("MSG202","処理済件数　：".$cnt."件\\nエラー件数    ：".$errcnt."件");
        }
    }

    /***********************/
    /* CSVファイルを配列へ */
    /***********************/
    function GetFileString() {
        $temp_arr = array();
        if (!is_uploaded_file($this->field["FILE"]["tmp_name"])) { //HTTP POSTによりアップロードされたファイルかどうかを調べる
            $this->warning = "ファイル名が不正です。";
        } else {
            $checkdata = array("APPLICANTDIV"  => array(1,   "入試制度"),
                               "EXAMNO"        => array(5,   "受検番号"),
                               "NAME"          => array(60,  "生徒氏名"),
                               "NAME_KANA"     => array(120, "氏名かな"),
                               "BIRTHDAY"      => array(10,  "生年月日"),
                               "SEX"           => array(1,   "性別"),
                               "FS_CD"         => array(7,   "出身学校"),
                               "FS_DAY"        => array(10,  "卒業年月日"),
                               "FS_GRDDIV"     => array(1,   "卒業区分"),
                               "ZIPCD"         => array(8,   "郵便番号"),
                               "ADDRESS1"      => array(75,  "住所1"),
                               "ADDRESS2"      => array(75,  "住所2"),
                               "GTELNO"        => array(14,  "保護者電話番号"),
                               "GNAME"         => array(60,  "保護者氏名"),
                               "WISHNO1"       => array(3,   "志望1"),
                               "WISHNO2"       => array(3,   "志望2"),
                               "WISHNO3"       => array(3,   "志望3"),
                               "WISHNO4"       => array(3,   "志望4"),
                               "WISHNO5"       => array(3,   "志望5"),
                               "WISHNO6"       => array(3,   "志望6"));
            $db = Query::dbCheckOut();
            $db->autoCommit(false);

            //エラーデータの削除
            $query = knjl014sQuery::deleteQueryErr();
            $result = $db->query($query);

            $fp = fopen ($this->field["FILE"]["tmp_name"], "r");
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
                    $buffer = trim(mb_convert_encoding($buffer, mb_http_output(), "SJIS-win"));
                    $tmp    = common::csv2array($buffer);

                    $data = array("APPLICANTDIV"  => (strlen(trim($tmp[0])))  ? sprintf("%01d",$tmp[0]) : "",
                                  "EXAMNO"        => (strlen(trim($tmp[1])))  ? sprintf("%05d",$tmp[1]) : "",
                                  "NAME"          => $tmp[2],
                                  "NAME_KANA"     => $tmp[3],
                                  "BIRTHDAY"      => $tmp[4],
                                  "SEX"           => (strlen(trim($tmp[5])))  ? sprintf("%01d",$tmp[5]) : "",
                                  "FS_CD"         => (strlen(trim($tmp[6])))  ? sprintf("%07d",$tmp[6]) : "",
                                  "FS_DAY"        => $tmp[7],
                                  "FS_GRDDIV"     => $tmp[8],
                                  "ZIPCD"         => $tmp[9],
                                  "ADDRESS1"      => $tmp[10],
                                  "ADDRESS2"      => $tmp[11],
                                  "GTELNO"        => $tmp[12],
                                  "GNAME"         => $tmp[13],
                                  "WISHNO1"       => (strlen(trim($tmp[14]))) ? sprintf("%03d",$tmp[14]) : "",
                                  "WISHNO2"       => (strlen(trim($tmp[15]))) ? sprintf("%03d",$tmp[15]) : "",
                                  "WISHNO3"       => (strlen(trim($tmp[16]))) ? sprintf("%03d",$tmp[16]) : "",
                                  "WISHNO4"       => (strlen(trim($tmp[17]))) ? sprintf("%03d",$tmp[17]) : "",
                                  "WISHNO5"       => (strlen(trim($tmp[18]))) ? sprintf("%03d",$tmp[18]) : "",
                                  "WISHNO6"       => (strlen(trim($tmp[19]))) ? sprintf("%03d",$tmp[19]) : "");

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

    /*****************/
    /*エラーチェック */
    /*****************/
    function errCheck($db, $data, $checkdata, $line) {
        $returnFlg = true;
        $hissuErrFlg     = false;    //必須エラーフラグ
        $existenceErrFlg = false;    //存在エラーフラグ
        $numericErrFlg   = false;    //数字エラーフラグ
        $lengthErrFlg    = false;    //レングスエラーフラグ
        $dateErrFlg      = false;    //日付エラーフラグ

        $hissuErrMsg     = "";       //必須エラーメッセージ
        $existenceErrMsg = "";       //存在エラーメッセージ
        $numericErrMsg   = "";       //数字エラーメッセージ
        $lengthErrMsg    = "";       //レングスエラーメッセージ
        $dateErrMsg      = "";       //日付エラーメッセージ

        $hissuSep     = "";
        $existenceSep = "";
        $numericSep   = "";
        $lengthSep    = "";
        $dateSep      = "";

        foreach ($checkdata as $key => $val) {
            //必須項目チェック
            if (in_array($key,array("EXAMNO"))) {
                if (!strlen(trim($data[$key]))) {
                    $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[1]);
                } else {
                    if ($this->field["SHORI_MEI"] == "1") { //1:更新 2:削除
                        if ($key == 'SEX' && 0 == $db->getOne(knjl014sQuery::sexCntSql($data[$key]))) {
                            $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[1]);
                        }
                        if ($key == 'FS_CD' && 0 == $db->getOne(knjl014sQuery::fs_cdCntSql($data[$key]))) {
                            $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[1]);
                        }
                        if ($key == 'FS_GRDDIV' && 0 == $db->getOne(knjl014sQuery::fs_grddivCntSql($data[$key]))) {
                            $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[1]);
                        }
                        if (preg_match('/^WISHNO/', $key) && 0 == $db->getOne(knjl014sQuery::wishnoCntSql($data[$key]))) {
                            $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[1]);
                        }
                    }
                }
            }

            //数値チェック
            if (in_array($key,array("EXAMNO","SEX","FS_CD","FS_GRDDIV","WISHNO1","WISHNO2","WISHNO3","WISHNO4","WISHNO5","WISHNO6"))) {
                if ($data[$key] != "" && !is_numeric($data[$key])) {
                    $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, $val[1]);
                }
            }

            if ($this->field["SHORI_MEI"] == "1") { //1:更新 2:削除
                //文字数チェック
                if (strlen($data[$key]) > $val[0]) {
                    $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[1]);
                }
            }

            //日付チェック
            if (in_array($key,array("BIRTHDAY","FS_DAY"))) {
                if ($data[$key] != "" && !$this->dateCheck($data[$key])) {
                    $returnFlg = $this->errSet($dateErrFlg, $dateErrMsg, "※日付:", $dateSep, $val[1]);
                }
            }
        }

        if (!$returnFlg) {
            $check_error = $hissuErrMsg.$numericErrMsg.$lengthErrMsg.$existenceErrMsg.$dateErrMsg;
            if (strlen($check_error) > 120) {
                $check_error = mb_strcut($check_error,0,115);
                $check_error .= "..他";
            }
            knjl014sQuery::insertQueryErr($db, $line, $check_error);
        }
        return $returnFlg;
    }

    /****************/
    /* 日付チェック */
    /****************/
    function dateCheck($date) {
        if ($date != "") {
            $date = common::DateConv1(str_replace("-","/",$date),4);
            if ($date) $date = str_replace("/","-",$date);
        }
        return $date;
    }

    /****************************/
    /* エラーメッセージをセット */
    /****************************/
    function errSet(&$errFlg, &$errMsg, $firstMsg, &$sep, $val) {
        if (!$errFlg) {
            $errMsg .= $firstMsg;
            $errFlg = true;
        }
        $errMsg .= $sep.$val;
        $sep = "/";
        return false;
    }

    /**************************/
    /* データの整合性チェック */
    /**************************/
    function IsDataMatched(&$data_arr) {
        //５千件を超えたら制限エラー
        if (get_count($data_arr) > 5000) {
            $this->setWarning("MSG915","処理件数を５千件以下にして下さい 処理件数：".get_count($data_arr)."件");
            return false;
        }
        return true;

    }

    /**************/
    /* ＣＳＶ出力 */
    /**************/
    function getDownloadModel() {
        //項目
        $header_err = array("MSGROW"        => "行番号",
                            "MSGREMARK"     => "メッセージ");

        $header     = array("APPLICANTDIV"  => "※入試制度",
                            "EXAMNO"        => "※受検番号",
                            "NAME"          => "生徒氏名",
                            "NAME_KANA"     => "氏名かな",
                            "BIRTHDAY"      => "生年月日",
                            "SEX"           => "性別",
                            "FS_CD"         => "出身学校",
                            "FS_DAY"        => "卒業年月日",
                            "FS_GRDDIV"     => "卒業区分",
                            "ZIPCD"         => "郵便番号",
                            "ADDRESS1"      => "住所1",
                            "ADDRESS2"      => "住所2",
                            "GTELNO"        => "保護者電話番号",
                            "GNAME"         => "保護者氏名",
                            "WISHNO1"       => "志望1",
                            "WISHNO2"       => "志望2",
                            "WISHNO3"       => "志望3",
                            "WISHNO4"       => "志望4",
                            "WISHNO5"       => "志望5",
                            "WISHNO6"       => "志望6",
                            "DUMMY"         => "DUMMY");

        $header_rei = array("APPLICANTDIV"  => "1",
                            "EXAMNO"        => "12345",
                            "NAME"          => "アルプ太郎",
                            "NAME_KANA"     => "あるぷたろう",
                            "BIRTHDAY"      => "2010/01/01",
                            "SEX"           => "1",
                            "FS_CD"         => "1234567",
                            "FS_DAY"        => "2010/01/01",
                            "FS_GRDDIV"     => "1",
                            "ZIPCD"         => "123-4567",
                            "ADDRESS1"      => "東京都",
                            "ADDRESS2"      => "",
                            "GTELNO"        => "123-456-7890",
                            "GNAME"         => "アルプ父",
                            "WISHNO1"       => "001",
                            "WISHNO2"       => "002",
                            "WISHNO3"       => "003",
                            "WISHNO4"       => "004",
                            "WISHNO5"       => "005",
                            "WISHNO6"       => "006",
                            "DUMMY"         => "DUMMY");

        $contents  = "";    //クリア

        $header_data = false;   //出力フラグ


        if ($this->field["OUTPUT"]=="1") {
            // ヘッダ出力
            $header_data = true;
            $contents .= implode($header, ",")."\n";
            $contents .= implode($header_rei, ",")."\n";
        } else if ($this->field["OUTPUT"]=="3") {
            // エラー出力
            $contents .= implode($header_err, ",")."\n";
        } else if ($this->field["OUTPUT"]=="4") {
            // データ出力
            if( $this->field["HEADER"]=="on" ) $contents .= implode($header, ",")."\n";
        }

        //ヘッダ以外
        if ($this->field["OUTPUT"] != "1") {
            $db = Query::dbCheckOut();

            if($this->field["OUTPUT"]=="3") {
                //エラー
                $query = knjl014sQuery::selectCsvErrQuery();
            } else if ($this->field["OUTPUT"]=="4") {
                //データ
                $query = knjl014sQuery::selectMainQuery($this);
            }

            //SQL文発行
            $result = $db->query($query);
            while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                if($this->field["OUTPUT"]=="4"){                //データ
                    $row["EXAMNO"] = (int) $row["EXAMNO"]; //頭０を非表示
                    $row["BIRTHDAY"] = str_replace("-", "/", $row["BIRTHDAY"]);
                    $row["FS_DAY"]   = str_replace("-", "/", $row["FS_DAY"]);
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
            if($this->field["OUTPUT"]=="3"){        //エラー
                $couse = "エラー出力";
            } else {                                //エラー以外
                $couse = "志願者基礎データ";
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
?>
