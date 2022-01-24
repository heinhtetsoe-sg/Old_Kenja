<?php
class knjl690iModel extends Model
{
    public $field  = array();
    public $data   = array();
    public $cmd;
    public $file;
    public $ObjYear;

    public function init()
    {
        $this->ObjYear = CTRL_YEAR+1;
        $this->field = array(
                            "OUTPUT"           =>  VARS::post("OUTPUT"),           //出力取込種別
                            "HEADER"           =>  VARS::post("HEADER"),           //ヘッダ有
                            "SHORI_MEI"        =>  VARS::post("SHORI_MEI"),         //処理名
                            "APPLICANTDIV"      =>  "2",                           //入試制度 (固定で、2:高校を登録)
                            "DESIREDIV"         =>  "1",                           //ENTEXAM_APPLICANTBASE_DATに入れる適当な値
                            "SHDIV"             =>  "1"                           //ENTEXAM_APPLICANTBASE_DATに入れる適当な値
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
        $this->header["MAJORCD"]                    = "※学科";
        $this->header["TESTDIV"]                    = "※入試区分";
        $this->header["HOPE_ORDER1"]                = "※志望順位1";
        $this->header["HOPE_ORDER2"]                = "志望順位2";
        $this->header["HOPE_ORDER3"]                = "志望順位3";
        $this->header["HOPE_ORDER4"]                = "志望順位4";
        $this->header["EXAMNO"]                     = "※受験番号";
        $this->header["NAME"]                       = "※漢字氏名";
        $this->header["NAME_KANA"]                  = "※カナ氏名";
        $this->header["BIRTHDAY"]                   = "生年月日";
        $this->header["SEX"]                        = "性別";
        $this->header["FS_CD"]                      = "中学校コード";
        $this->header["FS_GRDYEAR"]                 = "卒業見込";
        $this->header["GNAME"]                      = "保護者氏名";
        $this->header["GKANA"]                      = "保護者氏名（カナ）";
        $this->header["ZIPCD"]                      = "郵便番号";
        $this->header["ADDRESS1"]                   = "住所1";
        $this->header["ADDRESS2"]                   = "住所2";
        $this->header["TELNO"]                      = "電話番号";
        $this->header["ENTER_DORMITORY_HOPE"]       = "入寮希望";
        $this->header["SCHOLARSHIP_HOPE"]           = "奨学希望";
    }

    /********************/
    /*  ＣＳＶ取込処理  */
    /********************/
    public function &getExecModel()
    {
        //ファイルの文字列の取得
        $this->data_arr = array();
        $this->data_arr = $this->GetFileString();

        //データの整合性チェック
        if ($this->IsDataMatched($this->data_arr)) {
            $cnt = 0;
            if ($this->field["SHORI_MEI"] == "1") {
                $cnt = knjl690iQuery::updateQueryCsv($this, $this->data_arr);
            } elseif ($this->field["SHORI_MEI"] == "2") {
                $cnt = knjl690iQuery::deleteQueryCsv($this, $this->data_arr);
            }
            $db = Query::dbCheckOut();
            $errcnt = $db->getOne("SELECT COUNT(*) FROM W_CSVMSG_PRG_DAT WHERE PROGRAMID = '".PROGRAMID."' AND MSGREMARK NOT LIKE '%存在警告%' ");
            $result = $db->query("SELECT * FROM W_CSVMSG_PRG_DAT WHERE PROGRAMID = '".PROGRAMID."' AND MSGREMARK LIKE '%存在警告%' ");
            $warcnt = 0;
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                if (mb_substr_count($row['MSGREMARK'],'※','UTF-8') == 1) {
                    $warcnt++;
                }
                else{
                    $errcnt++;
                }
            }
            Query::dbCheckIn($db);

            $this->setMessage(MSG202, "処理済件数　：".$cnt."件\\n警告件数    ：".$warcnt."件\\nエラー件数    ：".$errcnt."件");
        }
    }

    /*************************/
    /*  CSVファイルを配列へ  */
    /*************************/
    public function GetFileString()
    {
        $temp_arr = array();
        if (!is_uploaded_file($this->field["FILE"]["tmp_name"])) { //HTTP POSTによりアップロードされたファイルかどうかを調べる
            $this->warning = "ファイル名が不正です。";
        } else {
            $db = Query::dbCheckOut();

            //チェック対象項目
            $checkdata = array();
            $checkdata["MAJORCD"]                    = array(1,   "HISSU", "INT",  "OTHER");
            $checkdata["TESTDIV"]                    = array(2,   "HISSU", "INT2", "TESTDIV");
            $checkdata["HOPE_ORDER1"]                = array(1,   "HISSU", "INT",  "COURSE");
            $checkdata["HOPE_ORDER2"]                = array(1,   "",      "INT",  "COURSE");
            $checkdata["HOPE_ORDER3"]                = array(1,   "",      "INT",  "COURSE");
            $checkdata["HOPE_ORDER4"]                = array(1,   "",      "INT",  "COURSE");
            $checkdata["EXAMNO"]                     = array(10,  "HISSU", "INT",  "");
            $checkdata["NAME"]                       = array(120, "HISSU", "",     "");
            $checkdata["NAME_KANA"]                  = array(240, "HISSU", "",     "");
            $checkdata["SEX"]                        = array(1,   "",      "INT",  "OTHER");
            $checkdata["FS_CD"]                      = array(7,   "",      "INT2", "FINS");
            $checkdata["FS_GRDYEAR"]                 = array(4,   "",      "INT",  "");
            $checkdata["GNAME"]                      = array(120, "",      "",     "");
            $checkdata["GKANA"]                      = array(120, "",      "",     "");
            $checkdata["ZIPCD"]                      = array(8,   "",      "",     "");
            $checkdata["ADDRESS1"]                   = array(300, "",      "",     "");
            $checkdata["ADDRESS2"]                   = array(300, "", "",     "");
            $checkdata["TELNO"]                      = array(14,  "",      "",     "");
            $checkdata["ENTER_DORMITORY_HOPE"]       = array(1,   "HISSU", "INT",  "OTHER");
            $checkdata["SCHOLARSHIP_HOPE"]           = array(1,   "HISSU", "INT",  "OTHER");
            
    
            //エラーチェック用設定取得
            $this->courseChkMst = array();
            $query = knjl690iQuery::getCoureChkSql($this);

            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                if (!array_key_exists($row["MAJORCD"], $this->courseChkMst)) {
                    $this->courseChkMst[$row["MAJORCD"]] = array();
                }
                $this->courseChkMst[$row["COURSE"]] = array("COURSE_TYPE" => $row["COURSE_TYPE"],
                                                            "MAJORCD"     => $row["MAJORCD"]);
            }

            $db->autoCommit(false);

            //エラーデータの削除
            $query = knjl690iQuery::deleteQueryErr();
            $result = $db->query($query);

            $fp = fopen($this->field["FILE"]["tmp_name"], "r");
            $i  = 0;
            $line = 1;  //行数
            $header = false;
            while (!feof($fp)) {
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
                    $checkcount = count($tmp);

                    $data = array();
                    $dataCnt = 0;
                    foreach ($this->header as $field => $label) {
                        $data[$field] = $tmp[$dataCnt];
                        if (in_array($field, array("HOPE_ORDER2", "HOPE_ORDER3", "HOPE_ORDER4")) && $data[$field] == "0") {
                            $data[$field] = "";
                        }
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
                                list($size, $hissu, $div, $exist) = $checkdata[$field];
                                //ゼロ埋めしてセット
                                if ($div == "INT2" && $data[$field] != "" && is_numeric($data[$field])) {
                                    $data[$field] = sprintf("%0{$size}d", $data[$field]);
                                }
                            }
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
    public function errCheck($db, $data, $checkdata, $line)
    {
        $returnFlg          = true;
        $returnFlg2         = true;
        $hissuErrFlg        = false;    //必須エラーフラグ
        $existenceErrFlg    = false;    //存在エラーフラグ
        $numericErrFlg      = false;    //数字エラーフラグ
        $lengthErrFlg       = false;    //レングスエラーフラグ
        $dateErrFlg        = false;    //日付エラーフラグ
        $courseErrFlg       = false;    //コース不整合エラーフラグ

        $hissuErrMsg        = "";       //必須エラーメッセージ
        $existenceErrMsg    = "";       //存在エラーメッセージ
        $numericErrMsg      = "";       //数字エラーメッセージ
        $lengthErrMsg       = "";       //レングスエラーメッセージ
        $dateErrMsg        = "";       //日付エラーメッセージ
        $courseErrMsg       = "";       //コース不整合エラーメッセージ

        $hissuSep           = "";
        $existenceSep       = "";
        $existenceSep       = "";
        $numericSep         = "";
        $lengthSep          = "";
        $dateSep           = "";
        $courseSep          = "";

        foreach ($this->header as $field => $label) {
            $label = str_replace("※", "", $label);

            if (in_array($field, array_keys($checkdata))) {
                list($size, $hissu, $div, $exist) = $checkdata[$field];

                //必須項目チェック
                if ($hissu == "HISSU" && !strlen(trim($data[$field]))) {
                    $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, " ※必須:", $hissuSep, $label);
                }

                //数値チェック
                if (($div == "INT" || $div == "INT2") && $data[$field] != "" && !is_numeric($data[$field])) {
                    $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, " ※数値:", $numericSep, $label);
                }

                //チェック用にゼロ埋め
                if ($div == "INT2" && $data[$field] != "" && is_numeric($data[$field])) {
                    $data[$field] = sprintf("%0{$size}d", $data[$field]);
                }

                //存在チェック
                if ($exist == "FINS") {
                    if (strlen($data[$field]) && 0 == $db->getOne(knjl690iQuery::fs_cdCntSql($data[$field]))) {
                        $returnFlg2 = $this->errSet($existenceErrFlg, $existenceErrMsg, " ※存在警告:", $existenceSep, $label);
                    }
                } elseif ($exist == "COURSE") {
                    if (strlen($data[$field]) && 0 == $db->getOne(knjl690iQuery::hopeCourseCdCntSql($this, $data[$field]))) {
                        $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, " ※存在:", $existenceSep, $label);
                    }
                } elseif ($exist == "TESTDIV") {
                    if (strlen($data[$field]) && 0 == $db->getOne(knjl690iQuery::testDivCntSql($this, $data[$field]))) {
                        $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, " ※存在:", $existenceSep, $label);
                    }
                } elseif ($exist == "OTHER") { //マスタ以外でチェックする項目用
                    $validValues = array();
                    if ($field == "MAJORCD") {
                        $validValues = array("1", "2");
                    } elseif ($field == "SEX") {
                        $validValues = array("1", "2");
                    } elseif ($field == "ENTER_DORMITORY_HOPE") {
                        $validValues = array("1", "2", "3");
                    } elseif ($field == "SCHOLARSHIP_HOPE") {
                        $validValues = array("1", "2");
                    }
                    
                    if (strlen($data[$field]) && !in_array($data[$field], $validValues)) {
                        $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, " ※存在:", $existenceSep, $label);
                    }
                } elseif (strlen($exist)) { //設定マスタ
                    if (strlen($data[$field]) && 0 == $db->getOne(knjl690iQuery::entexamSettingCntSql($this, $exist, $data[$field]))) {
                        $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, " ※存在:", $existenceSep, $label);
                    }
                }

                //取込学科とコースの整合性チェック
                if ($exist == "COURSE") {
                    $course = $data[$field];
                    if (strlen($course) && is_array($this->courseChkMst[$course]) && $data["MAJORCD"] != $this->courseChkMst[$course]["MAJORCD"]) {
                        $returnFlg = $this->errSet($courseErrFlg, $courseErrMsg, " ※学科と不一致:", $courseSep, $label);
                    }
                }

                //文字数チェック
                if (strlen($data[$field]) > $size) {
                    $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, " ※サイズ:", $lengthSep, $label);
                }
                //日付チェック
                if ($div == "DATE" && $data[$field] != "" && $this->date_rep($this->getHihunDate($data[$field])) == "") {
                    $returnFlg = $this->errSet($dateErrFlg, $dateErrMsg, " ※日付:", $dateSep, $label);
                }
            }
        }

        if (!$returnFlg || !$returnFlg2) {
            $check_error = $hissuErrMsg.$numericErrMsg.$lengthErrMsg.$existenceErrMsg.$dateErrMsg.$courseErrMsg;
            if (strlen($check_error) > 120) {
                $check_error = mb_strcut($check_error, 0, 115);
                $check_error .= "..他";
            }
            knjl690iQuery::insertQueryErr($db, $line, $check_error);
        }
        return $returnFlg;
    }

    //ハイフンあり日付に変換
    public function getHihunDate($preFormattedDate)
    {
        $y = substr($preFormattedDate, 0, 4);
        $m = substr($preFormattedDate, 4, 2);
        $d = substr($preFormattedDate, 6, 2);
        return $y."-".$m."-".$d;
    }

    //日付変換 項目チェック後に行う。
    public function date_rep($data)
    {
        if ($data != "") {
            $data = common::DateConv1(str_replace("-", "/", $data), 4);
            if ($data) {
                $data = str_replace("/", "-", $data);
            }
        }

        return $data;
    }

    /******************************/
    /*  エラーメッセージをセット  */
    /******************************/
    public function errSet(&$errFlg, &$errMsg, $firstMsg, &$sep, $val)
    {
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
    public function IsDataMatched(&$data_arr)
    {
        //５千件を超えたら制限エラー
        if (count($data_arr) > 5000) {
            $this->setWarning(MSG915, "処理件数を５千件以下にして下さい 処理件数：".count($data_arr)."件");
            return false;
        }
        return true;
    }

    /********************/
    /*  ＣＳＶ出力処理  */
    /********************/
    public function getDownloadModel()
    {
        $db = Query::dbCheckOut();

        //エラー項目
        $header_err = array("MSGROW"        => "行番号",
                            "MSGREMARK"     => "メッセージ");

        /****************/
        /*  ヘッダー例  */
        /****************/

        /* 関数 */
        $getMstString = function ($db, $query) {
            $rtnStr = "";
            $sep = "";
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $rtnStr .= $sep.$row["VALUE"].":".$row["LABEL"];
                $sep = " ";
            }

            return $rtnStr;
        };
        
        //「入試区分」例取得
        $query = knjl690iQuery::getTestdivMst($this);
        $testdivMstStr = $getMstString($db, $query);
        //「志望コース」例取得
        $query = knjl690iQuery::getCourseMst($this);
        $courseMstStr = $getMstString($db, $query);
        //「入寮希望」例取得
        $query = knjl690iQuery::getEntexamSettingMst($this, "L013");
        $nameL013MstStr = $getMstString($db, $query);

        $header_rei["MAJORCD"]                  = "1:普通科 2:工業科";
        $header_rei["TESTDIV"]                  = $testdivMstStr;
        $header_rei["HOPE_ORDER1"]              = $courseMstStr;
        $header_rei["HOPE_ORDER2"]              = $courseMstStr;
        $header_rei["HOPE_ORDER3"]              = $courseMstStr;
        $header_rei["HOPE_ORDER4"]              = $courseMstStr;
        $header_rei["EXAMNO"]                   = "数字10桁以内";
        $header_rei["NAME"]                     = "全角40文字";
        $header_rei["NAME_KANA"]                = "全角80文字";
        $header_rei["BIRTHDAY"]                 = "西暦8桁数字(例: 20040625)";
        $header_rei["SEX"]                      = "1:男　2:女";
        $header_rei["FS_CD"]                    = "数字7桁";
        $header_rei["FS_GRDYEAR"]               = "卒業見込西暦年度4桁";
        $header_rei["GNAME"]                    = "全角40文字";
        $header_rei["GKANA"]                    = "全角80文字";
        $header_rei["ZIPCD"]                    = "郵便番号(例: 811-0201)";
        $header_rei["ADDRESS1"]                 = "全角100文字";
        $header_rei["ADDRESS2"]                 = "全角100文字";
        $header_rei["TELNO"]                    = "電話番号(例: 092-606-1001)";
        $header_rei["ENTER_DORMITORY_HOPE"]     = "1:胆心寮希望 2:コスモス寮希望 3:希望なし";
        $header_rei["SCHOLARSHIP_HOPE"]         = "1:希望 2:希望なし";


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
        } elseif ($this->field["OUTPUT"] == "3") {
            // エラー出力
            $contents .= implode($header_err, ",")."\n";
        }

        /********************/
        /*  データ部分出力  */
        /********************/
        if ($this->field["OUTPUT"] != "1") {
            $query = "";
            if ($this->field["OUTPUT"] == "3") {
                //エラー
                $query = knjl690iQuery::selectCsvErrQuery();
            }

            //SQL文発行
            if ($query) {
                $result = $db->query($query);
                while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    if ($this->field["OUTPUT"] == "4") {     //データ

                        //カンマ変換
                        $row = str_replace("\r\n", "", str_replace(",", ".", $row));
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
        $AppName = $db->getOne(knjl690iQuery::getNameMst($this->ObjYear, "L003", $this->field["APPLICANTDIV"]));
        //入試区分名称
        $namecd1 = "L004";
        $TestdivName = $db->getOne(knjl690iQuery::getNameMst($this->ObjYear, $namecd1, $this->field["TESTDIV"]));
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
