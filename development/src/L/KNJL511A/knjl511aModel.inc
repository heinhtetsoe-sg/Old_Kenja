<?php

require_once('for_php7.php');

class knjl511aModel extends Model {
    var $field  = array();
    var $data   = array();
    var $cmd;
    var $file;
    var $ObjYear;

    function init() {
        $this->ObjYear = CTRL_YEAR+1;

        $this->field = array(
                            "OUTPUT"           =>  VARS::post("OUTPUT"),           //出力取込種別
                            "HEADER"           =>  VARS::post("HEADER"),           //ヘッダ有
                            "SHORI_MEI"        =>  VARS::post("SHORI_MEI"),         //処理名
                            "APPLICANTDIV"      =>  "1",                           //入試制度 (固定で、1:高校を登録)
                            "DESIREDIV"         =>  "1",                           //ENTEXAM_APPLICANTBASE_DATに入れる適当な値
                            "SHDIV"             =>  "1"                           //ENTEXAM_APPLICANTBASE_DATに入れる適当な値
                             );

        //データ取込時、データ未入力の場合は以下のデフォルト値を設定
        $this->defaultVal = array(
                            "SEX"               =>  "2",
                            "RELATIONSHIP"      =>  "1",
                            "FS_DAY"            =>  "{$this->ObjYear}-03-31" ,
                            "HOPE_COURSECODE"   =>  "0002"
        );

        $this->cmd = VARS::request("cmd");

        if (VARS::file("FILE")) {
            $this->field["FILE"] = VARS::file("FILE");      //ファイルのアップロード
            $this->field["FILE"]["name"] = mb_convert_encoding($this->field["FILE"]["name"], "SJIS-win", "UTF-8");
        }

        /**************/
        /*  ヘッダー  */
        /**************/
        $this->header = array();
        $this->header["ENTEXAMYEAR"]                = "※入試年度";
        $this->header["TESTDIV"]                    = "※入試区分";
        $this->header["EXAMNO"]                     = "※受験番号";
        $this->header["HOPE_COURSECODE"]            = "※志望区分";
        $this->header["NAME"]                       = "※氏名";
        $this->header["NAME_KANA"]                  = "※氏名かな";
        $this->header["SEX"]                        = "性別";
        $this->header["BIRTHDAY"]                   = "生年月日";
        $this->header["FS_DAY"]                     = "卒業年月日";
        $this->header["FS_CD"]                      = "出身学校コード";
        $this->header["ZIPCD"]                      = "郵便番号";
        $this->header["ADDRESS1"]                   = "住所";
        $this->header["ADDRESS2"]                   = "方書";
        $this->header["TELNO"]                      = "電話番号";
        $this->header["GNAME"]                      = "保護者氏名";
        $this->header["GKANA"]                      = "保護者氏名かな";
        $this->header["RELATIONSHIP"]               = "続柄";
        $this->header["GZIPCD"]                     = "保護者郵便番号";
        $this->header["GADDRESS1"]                  = "保護者住所";
        $this->header["GADDRESS2"]                  = "保護者方書";
        $this->header["GTELNO"]                     = "保護者電話番号";
    }

    /********************/
    /*  ＣＳＶ取込処理  */
    /********************/
    function &getExecModel() {
        //ファイルの文字列の取得
        $this->data_arr = array();
        $this->data_arr = $this->GetFileString();

        //データの整合性チェック
        if ( $this->IsDataMatched($this->data_arr) ) {
            $cnt = 0;
            if ($this->field["SHORI_MEI"] == "1") {
                $cnt = knjl511aQuery::updateQueryCsv($this, $this->data_arr);
            } else if ($this->field["SHORI_MEI"] == "2") {
                $cnt = knjl511aQuery::deleteQueryCsv($this, $this->data_arr);
            }
            $db = Query::dbCheckOut();
            $errcnt = $db->getOne("SELECT COUNT(*) FROM W_CSVMSG_PRG_DAT WHERE PROGRAMID = '".PROGRAMID."' ");
            Query::dbCheckIn($db);

            $this->setMessage("MSG202","処理済件数　：".$cnt."件\\nエラー件数    ：".$errcnt."件");
        }
    }

    /*************************/
    /*  CSVファイルを配列へ  */
    /*************************/
    function GetFileString() {
        $temp_arr = array();
        if (!is_uploaded_file($this->field["FILE"]["tmp_name"])) { //HTTP POSTによりアップロードされたファイルかどうかを調べる
            $this->warning = "ファイル名が不正です。";
        } else {
            $db = Query::dbCheckOut();

            //チェック対象項目
            $checkdata = array();
            $checkdata["ENTEXAMYEAR"]                = array(4,    "HISSU", "INT",      "DISPLAY");
            $checkdata["TESTDIV"]                    = array(1,    "HISSU", "INT",      "");
            $checkdata["EXAMNO"]                     = array(5,    "HISSU", "INT2",     "");
            $checkdata["HOPE_COURSECODE"]            = array(4,    "HISSU", "INT2",     "HOPE");
            $checkdata["NAME"]                       = array(120,  "HISSU", "",         "");
            $checkdata["NAME_KANA"]                  = array(120,  "HISSU", "",         "");
            $checkdata["SEX"]                        = array(1,    "",      "INT",      "Z002");
            $checkdata["BIRTHDAY"]                   = array(10,   "",      "DATE",     "");
            $checkdata["FS_DAY"]                     = array(10,   "",      "DATE",     "");
            $checkdata["FS_CD"]                      = array(4,    "",      "INT2",     "FINS");
            $checkdata["ZIPCD"]                      = array(8,    "",      "",         "");
            $checkdata["ADDRESS1"]                   = array(150,  "",      "",         "");
            $checkdata["ADDRESS2"]                   = array(150,  "",      "",         "");
            $checkdata["TELNO"]                      = array(14,   "",      "",         "");
            $checkdata["GNAME"]                      = array(120,  "",      "",         "");
            $checkdata["GKANA"]                      = array(120,  "",      "",         "");
            $checkdata["RELATIONSHIP"]               = array(2,    "",     "INT2",     "H201");
            $checkdata["GZIPCD"]                     = array(8,    "",      "",         "");
            $checkdata["GADDRESS1"]                  = array(150,  "",      "",         "");
            $checkdata["GADDRESS2"]                  = array(150,  "",      "",         "");
            $checkdata["GTELNO"]                     = array(14,   "",      "",         "");

            $db->autoCommit(false);

            //エラーデータの削除
            $query = knjl511aQuery::deleteQueryErr();
            $result = $db->query($query);

            $fp = fopen ($this->field["FILE"]["tmp_name"], "r");
            $i  = 0;
            $line = 1;  //行数
            $header = false;
            while (!feof ($fp)) {
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
                    $dataCnt = 0;
                    foreach ($this->header as $field => $label) {
                        $data[$field] = $tmp[$dataCnt];
                        $dataCnt++;
                    }

                    //項目数によるファイルチェック
                    if ($checkcount != $dataCnt) {
                        $this->warning = "CSVファイルを確認して下さい。\\nフォーマットが一致していません。";
                        return;
                    }

                    //エラーチェック
                    if ($this->errCheck($db, $data, $checkdata, $line)) {
                        foreach ($this->header as $field => $label) {
                            if (in_array($field, array_keys($checkdata))) {
                                list ($size, $hissu, $div, $exist) = $checkdata[$field];
                                //日付を変換してセット
                                if ($div == "DATE") {
                                    $data[$field] = $this->date_rep($data[$field]);
                                }
                                //ゼロ埋めしてセット
                                if ($div == "INT2" && $data[$field] != "" && is_numeric($data[$field])) {
                                    $data[$field] = sprintf("%0{$size}d", $data[$field]);
                                }
                                //改行コードに変換してセット
                                if ($div == "CRLF" && $data[$field] != "") {
                                    $data[$field] = str_replace("↓", "\n", $data[$field]);
                                }
                            }
                        }
                        //固定でセット
                        if ($data["APPLICANTDIV"] == '1') {
                            $data["SHDIV"]      = '1';
                        }

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

    /********************/
    /*  エラーチェック  */
    /********************/
    function errCheck($db, $data, $checkdata, $line) {
        $returnFlg          = true;
        $hissuErrFlg        = false;    //必須エラーフラグ
        $existenceErrFlg    = false;    //存在エラーフラグ
        $numericErrFlg      = false;    //数字エラーフラグ
        $lengthErrFlg       = false;    //レングスエラーフラグ
        $dateErrFlg        = false;    //日付エラーフラグ

        $hissuErrMsg        = "";       //必須エラーメッセージ
        $existenceErrMsg    = "";       //存在エラーメッセージ
        $numericErrMsg      = "";       //数字エラーメッセージ
        $lengthErrMsg       = "";       //レングスエラーメッセージ
        $dateErrMsg        = "";       //日付エラーメッセージ

        $hissuSep           = "";
        $existenceSep       = "";
        $existenceSep       = "";
        $numericSep         = "";
        $lengthSep          = "";
        $dateSep           = "";

        foreach ($this->header as $field => $label) {
            $label = str_replace("※", "", $label);

            if (in_array($field, array_keys($checkdata))) {

                list ($size, $hissu, $div, $exist) = $checkdata[$field];

                //必須項目チェック
                if ($hissu == "HISSU" && !strlen(trim($data[$field]))) {
                    $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $label);
                }

                //数値チェック
                if (($div == "INT" || $div == "INT2") && $data[$field] != "" && !is_numeric($data[$field])) {
                    $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, $label);
                }

                //チェック用にゼロ埋め
                if ($div == "INT2" && $data[$field] != "" && is_numeric($data[$field])) {
                    $data[$field] = sprintf("%0{$size}d", $data[$field]);
                }

                //存在チェック
                if ($exist == "DISPLAY") {
                    if (($field == "ENTEXAMYEAR" && $this->ObjYear != $data[$field])) {
                        $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $label);
                    }
                } else if ($exist == "FINS") {
                    if (strlen($data[$field]) && 0 == $db->getOne(knjl511aQuery::fs_cdCntSql($this->ObjYear, $data[$field]))) {
                        $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $label);
                    }
                } else if ($exist == "HOPE") {
                    if (strlen($data[$field]) && 0 == $db->getOne(knjl511aQuery::hopeCourseCdCntSql($this->ObjYear, $data[$field]))) {
                        $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $label);
                    }
                } else if (strlen($exist)) {
                    if (strlen($data[$field]) && 0 == $db->getOne(knjl511aQuery::nameCntSql($exist, $data[$field]))) {
                        $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $label);
                    }
                }

                //文字数チェック
                if (strlen($data[$field]) > $size) {
                    $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $label);
                }

                //日付チェック
                if ($div == "DATE" && $data[$field] != "" && $this->date_rep($data[$field]) == "") {
                    $returnFlg = $this->errSet($dateErrFlg, $dateErrMsg, "※日付:", $dateSep, $label);
                }

            }
        }

        if (!$returnFlg) {
            $check_error = $hissuErrMsg.$numericErrMsg.$lengthErrMsg.$existenceErrMsg.$dateErrMsg;
            if (strlen($check_error) > 120) {
                $check_error = mb_strcut($check_error,0,115);
                $check_error .= "..他";
            }
            knjl511aQuery::insertQueryErr($db, $line, $check_error);
        }
        return $returnFlg;
    }

    //日付変換 項目チェック後に行う。
    function date_rep($data) {
        if ($data != "") {
            $data = common::DateConv1(str_replace("-","/",$data),4);
            if ($data) $data = str_replace("/","-",$data);
        }

        return $data;
    }

    /******************************/
    /*  エラーメッセージをセット  */
    /******************************/
    function errSet(&$errFlg, &$errMsg, $firstMsg, &$sep, $val) {
        if (!$errFlg) {
            $errMsg .= $firstMsg;
            $errFlg = true;
        }
        $errMsg .= $sep.$val;
        $sep = "/";
        return false;
    }

    /****************************/
    /*  データの整合性チェック  */
    /****************************/
    function IsDataMatched(&$data_arr) {
        //５千件を超えたら制限エラー
        if (get_count($data_arr) > 5000) {
            $this->setWarning("MSG915","処理件数を５千件以下にして下さい 処理件数：".get_count($data_arr)."件");
            return false;
        }
        return true;
    }

    /********************/
    /*  ＣＳＶ出力処理  */
    /********************/
    function getDownloadModel() {
        $db = Query::dbCheckOut();

        //エラー項目
        $header_err = array("MSGROW"        => "行番号",
                            "MSGREMARK"     => "メッセージ");

        /****************/
        /*  ヘッダー例  */
        /****************/

        //志望区分
        $hopeCourseCd = implode(' ', $db->getCol(knjl511aQuery::getHeaderReiHopeCourseCd($this, $this->field["TESTDIV"])));
        //入試区分
        $testdiv  = implode(' ', $db->getCol(knjl511aQuery::getHeaderReiNameMst($this->ObjYear, 'L004', "NAMECD2 || ':' || NAME1")));
        $yOldE =  "15";   //入学年令
        //性別
        $sex = implode(' ', $db->getCol(knjl511aQuery::getHeaderReiNameMst($this->ObjYear, "Z002", "NAMECD2 || ':' || NAME1")));

        $header_rei = array();
        $header_rei["ENTEXAMYEAR"]              = $this->ObjYear;
        $header_rei["TESTDIV"]                  = $testdiv;
        $header_rei["EXAMNO"]                   = '12345';
        $header_rei["HOPE_COURSECODE"]          = $hopeCourseCd;
        $header_rei["HOPE_COURSECODE_NAME"]     = '入力の必要なし';     //表示のみ
        $header_rei["NAME"]                     = 'アルプ　太郎';
        $header_rei["NAME_KANA"]                = 'あるぷ　たろう';
        $header_rei["SEX"]                      = $sex;
        $header_rei["SEXNAME"]                  = '入力の必要なし';     //表示のみ
        $header_rei["BIRTHDAY"]                 = date('Y-m-d', strtotime(CTRL_DATE." -{$yOldE} years"));
        $header_rei["FS_DAY"]                   = $this->ObjYear.'-03-31';
        $header_rei["FS_CD"]                    = '1234';
        $header_rei["FS_NAME"]                  = '入力の必要なし';     //表示のみ
        $header_rei["ZIPCD"]                    = "123-4567";
        $header_rei["ADDRESS1"]                 = "全角50文字";
        $header_rei["ADDRESS2"]                 = "全角50文字";
        $header_rei["TELNO"]                    = "012-3456-7890";
        $header_rei["GNAME"]                    = "アルプ　保護者";
        $header_rei["GKANA"]                    = "あるぷ　ほごしゃ";
        $header_rei["RELATIONSHIP"]             = "1 : 父親";
        $header_rei["RELATIONSHIPNAME"]         = '入力の必要なし';     //表示のみ
        $header_rei["GZIPCD"]                   = "123-4567";
        $header_rei["GADDRESS1"]                = "全角50文字";
        $header_rei["GADDRESS2"]                = "全角50文字";
        $header_rei["GTELNO"]                   = "012-3456-7890";

        /**********************/
        /*  ヘッダー部分出力  */
        /**********************/
        $contents  = "";        //クリア
        $header_data = false;   //出力フラグ
        if ($this->field["OUTPUT"] == "1") {
            // ヘッダ出力
            $header_data = true;
            $contents .= implode($this->header, ',')."\n";
            $sep = '';
            foreach ($this->header as $field => $label) {
                $contents .= $sep.$header_rei[$field];
                $sep = ',';
            }
            $contents .= "\n";
        } else if ($this->field["OUTPUT"] == "3") {
            // エラー出力
            $contents .= implode($header_err, ",")."\n";
        } else if ($this->field["OUTPUT"] == "4") {
            // データ出力
            if ($this->field["HEADER"] == "on") {
                $contents .= implode($this->header, ",")."\n";
            }
        }

        /********************/
        /*  データ部分出力  */
        /********************/
        if ($this->field["OUTPUT"] != "1") {
            $query = "";
            if ($this->field["OUTPUT"] == "3") {
                //エラー
                $query = knjl511aQuery::selectCsvErrQuery();
            } else if ($this->field["OUTPUT"] == "4") {
                $query = knjl511aQuery::selectMainQuery($this);
            }

            //日付変換項目一覧
            $date_array = array("BIRTHDAY");
            //改行コード変換項目一覧
            $textarea_array = array("REMARK1", "REMARK2");

            //SQL文発行
            if ($query) {
                $result = $db->query($query);
                while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    if ($this->field["OUTPUT"] == "4") {     //データ
/*
                        //日付変換
                        foreach ($date_array as $key) {
                            $row[$key] = str_replace("-", "/", $row[$key]);
                        }
*/
                        //カンマ変換
                        $row = str_replace("\r\n","",str_replace(",",".",$row));
                        $array = $this->header;
                    } else {
                        $array = $header_err;
                    }

                    //ヘッダ順に値をセット
                    $sep = "";
                    foreach ($array as $field => $label) {
                        $contents .= $sep.$row[$field];
                        $sep = ",";
                    }
                    $contents .= "\n";

                    $header_data = true;
                }
                $result->free();
            }
        }

        /**************************/
        /*  ファイル名用名称取得  */
        /**************************/
        //入試制度名称
        $AppName = $db->getOne(knjl511aQuery::getNameMst($this->ObjYear, "L003", $this->field["APPLICANTDIV"]));
        //入試区分名称
        $namecd1 = "L004";
        $TestdivName = $db->getOne(knjl511aQuery::getNameMst($this->ObjYear, $namecd1, $this->field["TESTDIV"]));
        //ファイル名
        $title = $this->ObjYear.'年度_'.$AppName.'_'.'志願者データ';

        Query::dbCheckIn($db);
        //ＣＳＶ出力
        if ($header_data) {
            //エラー出力
            if ($this->field["OUTPUT"] == "3") {
                $filename = sprintf("%s.csv", "エラー出力");
            //エラー出力以外
            } else {
                $filename = sprintf("%s.csv", $title);
            }
            common::downloadContents($contents, $filename);
            return true;
        } else {
            $this->warning = "該当データは、ありません。";
            return false;
        }
    }
}
?>
