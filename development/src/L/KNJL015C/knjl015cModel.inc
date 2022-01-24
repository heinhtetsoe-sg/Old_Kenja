<?php

require_once('for_php7.php');
class knjl015cModel extends Model
{
    public $field      = array();
    public $data       = array();
    public $cmd;
    public $file;
    public $control; //コントロールマスタ
    public $staffcd; //登録者コード
    public $fieldSize = array();
    public $test_year;
    public $lastColumn;//CSVの最後の列の値

    public function init()
    {
        common::GetControlMaster_Fnc($this->control);

        $this->field = array("OUTPUT"        =>  VARS::post("OUTPUT"),        //出力取込種別
                             "APPLICANTDIV"  =>  VARS::post("APPLICANTDIV"),  //入試制度
                             "TESTDIV"       =>  VARS::post("TESTDIV"),       //入試制度
                             "HEADER"        =>  VARS::post("HEADER"),        //ヘッダ有
                             "SHORI_MEI"     =>  VARS::post("SHORI_MEI")      //処理名
                             );

        $this->test_year = CTRL_YEAR + 1;
        $this->cmd = VARS::request("cmd");

        if (VARS::file("FILE")) {
            $this->field["FILE"] = VARS::file("FILE");        //ファイルのアップロード
            $this->field["FILE"]["name"] = mb_convert_encoding($this->field["FILE"]["name"], "SJIS-win", "UTF-8");
        }

        //学校コード取得
        $schoolcd = knjl015cQuery::getSchoolcd();
        $this->isWakayama   = ($schoolcd == "30300049001") ? true : false;
        $this->isGojo       = ($schoolcd == "30290053001") ? true : false;
        $this->isCollege    = ($schoolcd == "30290086001") ? true : false;

        //プロパティファイル
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
        $this->lastColumn = 'DUMMY';
        if ($this->Properties["csv_LastColumn"] != '') {
            $this->lastColumn = $this->Properties["csv_LastColumn"];
        }
    }

    /***************/
    /* CSVの取込み */
    /***************/
    public function &getExecModel()
    {
        //ファイルの文字列の取得
        $this->data_arr = array();
        $this->data_arr = $this->getFileString();

        //データの整合性チェック
        if ($this->isDataMatched($this->data_arr)) {
            if ($this->field["SHORI_MEI"] == "1") { //1:更新 2:削除
                $cnt = knjl015cQuery::udpateQueryCsv($this, $this->data_arr);
            } else {
//                $cnt = knjl015cQuery::DeleteQueryCsv($this,$this->data_arr);
            }
            $db = Query::dbCheckOut();
            $errcnt = $db->getOne("SELECT COUNT(*) FROM W_CSVMSG_DAT");
            Query::dbCheckIn($db);

            $this->setMessage("MSG202", "処理済件数　：".$cnt."件\\nエラー件数    ：".$errcnt."件");
        }
    }

    /***********************/
    /* CSVファイルを配列へ */
    /***********************/
    public function getFileString()
    {
        $temp_arr = array();
        if (!is_uploaded_file($this->field["FILE"]["tmp_name"])) { //HTTP POSTによりアップロードされたファイルかどうかを調べる
            $this->warning = "ファイル名が不正です。";
        } else {
            $sd_flg_name = ($this->isGojo) ? "カレッジ併願" : "移行希望フラグ";
            $checkdata =  array("APPLICANTDIV"          => array(1,   "※入試制度"),
                                "TESTDIV"               => array(1,   "※入試区分"),
                                "EXAMNO"                => array(4,   "※受験番号"),
                                "NAME_KANA"             => array(120, "かな"),
                                "NAME"                  => array(60,  "氏名"),
                                "SHDIV"                 => array(1,   "※専併コード"),
                                "TESTDIV1"              => array(1,   "第２志望"),
                                "SEX"                   => array(1,   "性別"),
                                "BIRTHDAY"              => array(10,  "生年月日"),
                                "ZIPCD"                 => array(8,   "郵便番号"),
                                "PREF_CD"               => array(2,   "県別"),
                                "ADDRESS1"              => array(75,  "現住所1"),
                                "ADDRESS2"              => array(75,  "現住所2"),
                                "EDBOARDCD"             => array(6,   "教育委員会コード"),
                                "TELNO"                 => array(14,  "電話番号"),
                                "FS_CD"                 => array(7,   "出身学校コード"),
                                "FS_YM"                 => array(7,   "卒業年月"),
                                "GKANA"                 => array(120, "保護者かな"),
                                "GNAME"                 => array(60,  "保護者氏名"),
                                "RELATIONSHIP"          => array(2,   "続柄コード"),
                                "GZIPCD"                => array(8,   "保護者郵便番号"),
                                "GPREF_CD"              => array(2,   "保護者県別"),
                                "GADDRESS1"             => array(75,  "保護者現住所1"),
                                "GADDRESS2"             => array(75,  "保護者現住所2"),
                                "GTELNO"                => array(14,  "保護者電話番号"),
                                "RECOM_EXAMNO"          => array(4,   "推薦受験番号"),
                                "SELECT_SUBCLASS_DIV"   => array(1,   "選択受験科目"),
                                "SHIFT_DESIRE_FLG"      => array(1,   $sd_flg_name),
                                "SHIFT_DESIRE_FLG1"     => array(1,   "五条併願"),
                                "SHIFT_DESIRE_FLG2"     => array(1,   "和歌山併願"),
                                "PRISCHOOLCD"           => array(7,   "塾コード"),
                                "REMARK1"               => array(60,  "備考1"),
                                "REMARK2"               => array(120, "備考2"),
                                "BUS_USE"               => array(1,   "学園バス"),
                                "STATIONDIV"            => array(1,   "乗降地"),
                                "BUS_USER_COUNT"        => array(2,   "ご利用人数"),
                                "PRE_RECEPTNO"          => array(5,   "プレテスト"),
                                "DORMITORY_FLG"         => array(1,   "入寮"),
                                "PERSONAL_FLG"          => array(1,   "個人成績"),
                                "SUB_ORDER"             => array(1,   "受験型")
                                );

            $db = Query::dbCheckOut();
            $db->autoCommit(false);

            //エラーデータの削除
            $query = knjl015cQuery::deleteQueryErr();
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

                    $data = array();
                    $data["APPLICANTDIV"]         = (strlen(trim($tmp[0])))  ? sprintf("%01d", $tmp[0]) : "";
                    $data["TESTDIV"]              = (strlen(trim($tmp[1])))  ? $tmp[1] : "";
                    $data["EXAMNO"]               = (strlen(trim($tmp[2])))  ? sprintf("%04d", $tmp[2]) : "";
                    $data["NAME_KANA"]            = $tmp[3];
                    $data["NAME"]                 = $tmp[4];
                    $data["SHDIV"]                = (strlen(trim($tmp[5])))  ? $tmp[5] : "";
                    $data["TESTDIV1"]             = $tmp[6];
                    $data["SEX"]                  = (strlen(trim($tmp[7])))  ? sprintf("%01d", $tmp[7]) : "";
                    $data["BIRTHDAY"]             = $tmp[8];
                    $data["ZIPCD"]                = $tmp[9];
                    $data["PREF_CD"]              = (strlen(trim($tmp[10])))  ? sprintf("%02d", $tmp[10]) : "";
                    $data["ADDRESS1"]             = $tmp[11];
                    $data["ADDRESS2"]             = $tmp[12];
                    $data["EDBOARDCD"]            = $tmp[13];
                    $data["TELNO"]                = $tmp[14];
                    $data["FS_CD"]                = (strlen(trim($tmp[15])))  ? sprintf("%07d", $tmp[15]) : "";
                    $data["FS_YM"]                = $tmp[16];
                    $data["GKANA"]                = $tmp[17];
                    $data["GNAME"]                = $tmp[18];
                    $data["RELATIONSHIP"]         = (strlen(trim($tmp[19])))  ? sprintf("%02d", $tmp[19]) : "";
                    $data["GZIPCD"]               = $tmp[20];
                    $data["GPREF_CD"]             = (strlen(trim($tmp[21])))  ? sprintf("%02d", $tmp[21]) : "";
                    $data["GADDRESS1"]            = $tmp[22];
                    $data["GADDRESS2"]            = $tmp[23];
                    $data["GTELNO"]               = $tmp[24];
                    $data["RECOM_EXAMNO"]         = (strlen(trim($tmp[25])))  ? sprintf("%04d", $tmp[25]) : "";
                    $data["SELECT_SUBCLASS_DIV"]  = $tmp[26];
                    $tmpCnt = 26;
                    if ($this->isCollege) {
                        $data["SHIFT_DESIRE_FLG1"]    = $tmp[++$tmpCnt];
                        $data["SHIFT_DESIRE_FLG2"]    = $tmp[++$tmpCnt];
                    } else {
                        $data["SHIFT_DESIRE_FLG"]     = $tmp[++$tmpCnt];
                    }
                    $data["PRISCHOOLCD"]          = (strlen(trim($tmp[++$tmpCnt])))  ? sprintf("%04d", $tmp[$tmpCnt]) : "";
                    $data["REMARK1"]              = $tmp[++$tmpCnt];
                    $data["REMARK2"]              = $tmp[++$tmpCnt];
                    $data["BUS_USE"]              = $tmp[++$tmpCnt];
                    $data["STATIONDIV"]           = $tmp[++$tmpCnt];
                    $data["BUS_USER_COUNT"]       = $tmp[++$tmpCnt];
                    $data["PRE_RECEPTNO"]         = $tmp[++$tmpCnt];
                    $data["DORMITORY_FLG"]        = $tmp[++$tmpCnt];
                    $data["PERSONAL_FLG"]         = $tmp[++$tmpCnt];
                    if ($this->isCollege) {
                        $data["SUB_ORDER"]            = $tmp[++$tmpCnt];
                    }

                    //エラーチェック
                    if ($this->errCheck($db, $data, $checkdata, $line)) {
                        foreach ($checkdata as $key => $val) {
                            //日付を変換してセット
                            if (in_array($key, array("BIRTHDAY", "FS_YM"))) {
                                $day = ($key == "FS_YM") ? "/01" : "";
                                $data[$key] = $this->dateRep($data[$key].$day);
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

    /*****************/
    /*エラーチェック */
    /*****************/
    public function errCheck($db, $data, $checkdata, $line)
    {
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
        $existenceSep = "";
        $numericSep   = "";
        $lengthSep    = "";

        foreach ($checkdata as $key => $val) {
            //必須項目チェック
            if (in_array($key, array("APPLICANTDIV","TESTDIV","EXAMNO","SHDIV"))) {
                if (!strlen(trim($data[$key]))) {
                    $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[1]);
                }
            }

            //存在チェック
            if ($this->field["SHORI_MEI"] == "1") { //1:更新 2:削除
                if ($key == 'APPLICANTDIV' && 0 == $db->getOne(knjl015cQuery::getNameMstCnt("L003", $data[$key]))) {
                    $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[1]);
                }
                if ($key == 'TESTDIV' && 0 == $db->getOne(knjl015cQuery::getNameMstCnt("L004", $data[$key]))) {
                    $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[1]);
                }
                if ($key == 'SHDIV' && 0 == $db->getOne(knjl015cQuery::getNameMstCnt("L006", $data[$key]))) {
                    $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[1]);
                }
                if ($key == 'RELATIONSHIP' && strlen($data[$key]) && 0 == $db->getOne(knjl015cQuery::getNameMstCnt("H201", $data[$key]))) {
                    $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[1]);
                }
                if ($key == 'SEX' && strlen($data[$key]) && 0 == $db->getOne(knjl015cQuery::getNameMstCnt("Z002", $data[$key]))) {
                    $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[1]);
                }
                if ($key == 'EDBOARDCD' && strlen($data[$key]) && 0 == $db->getOne(knjl015cQuery::edboardcdCntSql($data[$key]))) {
                    $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[1]);
                }
                if ($key == 'FS_CD' && strlen($data[$key]) && 0 == $db->getOne(knjl015cQuery::fsCdCntSql($data[$key]))) {
                    $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[1]);
                }
                if (preg_match('/^G?PREF_CD$/', $key) && strlen($data[$key]) && 0 == $db->getOne(knjl015cQuery::prefCdCntSql($data[$key]))) {
                    $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[1]);
                }
                if ($key == 'SELECT_SUBCLASS_DIV' && strlen($data[$key]) && 0 == $db->getOne(knjl015cQuery::getNameMstCnt("L009", $data[$key]))) {
                    $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[1]);
                }
                if ($key == 'PRISCHOOLCD' && strlen($data[$key]) && 0 == $db->getOne(knjl015cQuery::prischoolcdCntSql($data[$key]))) {
                    $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[1]);
                }
                if (in_array($key, array("SHIFT_DESIRE_FLG","SHIFT_DESIRE_FLG1","SHIFT_DESIRE_FLG2","BUS_USE","DORMITORY_FLG","PERSONAL_FLG"))) {
                    if (strlen($data[$key]) && $data[$key] != '1') {
                        $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[1]);
                    }
                }
                if (in_array($key, array("STATIONDIV","BUS_USER_COUNT"))) {
                    if (strlen($data[$key]) && $data["BUS_USE"] != "1") {
                        $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[1]);
                    }
                }
                if ($key == 'STATIONDIV' && strlen($data[$key]) && !in_array($data[$key], array(1,2,3))) {
                    $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[1]);
                }
                if ($key == 'SUB_ORDER' && strlen($data[$key]) && !in_array($data[$key], array(1,2))) {
                    $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[1]);
                }
                if ($key == 'TESTDIV1' && strlen($data[$key]) && !(in_array($data["APPLICANTDIV"]."-".$data["TESTDIV"], array("2-3")) && $data[$key] == '1')) {
                    $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[1]);
                }
            }

            //数値チェック
            if (in_array($key, array("EXAMNO", "RECOM_EXAMNO"))) {
                if ($data[$key] != "" && !is_numeric($data[$key])) {
                    $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, $val[1]);
                }
            }

            if ($this->field["SHORI_MEI"] == "1") { //1:更新 2:削除
                //文字数チェック
                if (strlen($data[$key]) > $val[0]) {
                    $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[1]);
                }

                //日付チェック
                if (in_array($key, array("BIRTHDAY", "FS_YM"))) {
                    $day = ($key == "FS_YM") ? "/01" : "";
                    if ($data[$key] != "" && $this->dateRep($data[$key].$day) == "") {
                        $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[1]);
                    }
                }
            }
        }

        if (!$returnFlg) {
            $check_error = $hissuErrMsg.$numericErrMsg.$lengthErrMsg.$existenceErrMsg;
            if (strlen($check_error) > 120) {
                $check_error = mb_strcut($check_error, 0, 115);
                $check_error .= "..他";
            }
            knjl015cQuery::insertQueryErr($db, $line, $check_error);
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

    /****************************/
    /* エラーメッセージをセット */
    /****************************/
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

    /**************************/
    /* データの整合性チェック */
    /**************************/
    public function isDataMatched(&$data_arr)
    {
        //５千件を超えたら制限エラー
        if (get_count($data_arr) > 5000) {
            $this->setWarning("MSG915", "処理件数を５千件以下にして下さい 処理件数：".get_count($data_arr)."件");
            return false;
        }
        return true;
    }

    /**************/
    /* ＣＳＶ出力 */
    /**************/
    public function getDownloadModel()
    {
        //項目
        $header_err = array("MSGROW"        => "行番号",
                            "MSGREMARK"     => "メッセージ");

        $header = $header_rei = array();

        //ヘッダ
        $header["APPLICANTDIV"]         = "※入試制度";
        $header["TESTDIV"]              = "※入試区分";
        $header["EXAMNO"]               = "※受験番号";
        $header["NAME_KANA"]            = "かな";
        $header["NAME"]                 = "氏名";
        $header["SHDIV"]                = "※専併コード";
        $header["TESTDIV1"]             = "第２志望";
        $header["SEX"]                  = "性別";
        $header["BIRTHDAY"]             = "生年月日";
        $header["ZIPCD"]                = "郵便番号";
        $header["PREF_CD"]              = "県別";
        $header["ADDRESS1"]             = "現住所1";
        $header["ADDRESS2"]             = "現住所2";
        $header["EDBOARDCD"]            = "教育委員会コード";
        $header["TELNO"]                = "電話番号";
        $header["FS_CD"]                = "出身学校コード";
        $header["FS_YM"]                = "卒業年月";
        $header["GKANA"]                = "保護者かな";
        $header["GNAME"]                = "保護者氏名";
        $header["RELATIONSHIP"]         = "続柄コード";
        $header["GZIPCD"]               = "保護者郵便番号";
        $header["GPREF_CD"]             = "保護者県別";
        $header["GADDRESS1"]            = "保護者現住所1";
        $header["GADDRESS2"]            = "保護者現住所2";
        $header["GTELNO"]               = "保護者電話番号";
        $header["RECOM_EXAMNO"]         = "推薦受験番号";
        $header["SELECT_SUBCLASS_DIV"]  = "選択受験科目";
        if ($this->isCollege) {
            $header["SHIFT_DESIRE_FLG1"]    = "五条併願";
            $header["SHIFT_DESIRE_FLG2"]    = "和歌山併願";
        } else {
            $header["SHIFT_DESIRE_FLG"]     = ($this->isGojo) ? "カレッジ併願" : "移行希望フラグ";
        }
        $header["PRISCHOOLCD"]          = "塾コード";
        $header["REMARK1"]              = "備考1";
        $header["REMARK2"]              = "備考2";
        $header["BUS_USE"]              = "学園バス";
        $header["STATIONDIV"]           = "乗降地";
        $header["BUS_USER_COUNT"]       = "ご利用人数";
        $header["PRE_RECEPTNO"]         = "プレテスト";
        $header["DORMITORY_FLG"]        = "入寮";
        $header["PERSONAL_FLG"]         = "個人成績";
        if ($this->isCollege) {
            $header["SUB_ORDER"]            = "受験型";
        }
        $header[$this->lastColumn]      = $this->lastColumn;

        //ヘッダ例
        $header_rei["APPLICANTDIV"]         = "1";
        $header_rei["TESTDIV"]              = "1";
        $header_rei["EXAMNO"]               = "1234";
        $header_rei["NAME_KANA"]            = "あるぷたろう";
        $header_rei["NAME"]                 = "アルプ太郎";
        $header_rei["SHDIV"]                = "1";
        $header_rei["TESTDIV1"]             = "1:希望する";
        $header_rei["SEX"]                  = "1";
        $header_rei["BIRTHDAY"]             = "2010/01/01";
        $header_rei["ZIPCD"]                = "123-4567";
        $header_rei["PREF_CD"]              = "12";
        $header_rei["ADDRESS1"]             = "東京都";
        $header_rei["ADDRESS2"]             = "";
        $header_rei["EDBOARDCD"]            = "123456";
        $header_rei["TELNO"]                = "12-3456-7890";
        $header_rei["FS_CD"]                = "1234567";
        $header_rei["FS_YM"]                = $this->test_year."/03";
        $header_rei["GKANA"]                = "あるぷちち";
        $header_rei["GNAME"]                = "アルプ父";
        $header_rei["RELATIONSHIP"]         = "01";
        $header_rei["GZIPCD"]               = "123-4567";
        $header_rei["GPREF_CD"]             = "12";
        $header_rei["GADDRESS1"]            = "東京都";
        $header_rei["GADDRESS2"]            = "";
        $header_rei["GTELNO"]               = "123-456-7890";
        $header_rei["RECOM_EXAMNO"]         = "1234";
        $header_rei["SELECT_SUBCLASS_DIV"]  = "1";
        if ($this->isCollege) {
            $header_rei["SHIFT_DESIRE_FLG1"]    = "1";
            $header_rei["SHIFT_DESIRE_FLG2"]    = "1";
        } else {
            $header_rei["SHIFT_DESIRE_FLG"]     = "1";
        }
        $header_rei["PRISCHOOLCD"]          = "1234567";
        $header_rei["REMARK1"]              = "備考1";
        $header_rei["REMARK2"]              = "備考2";
        $header_rei["BUS_USE"]              = "1";
        $header_rei["STATIONDIV"]           = "1";
        $header_rei["BUS_USER_COUNT"]       = "12345";
        $header_rei["PRE_RECEPTNO"]         = "12";
        $header_rei["DORMITORY_FLG"]        = "1";
        $header_rei["PERSONAL_FLG"]         = "1";
        if ($this->isCollege) {
            $header_rei["SUB_ORDER"]            = "1:Ⅰ型、2:Ⅱ型";
        }
        $header_rei[$this->lastColumn]      = $this->lastColumn;

        $contents  = "";    //クリア

        $header_data = false;   //出力フラグ


        if ($this->field["OUTPUT"]=="1") {
            // ヘッダ出力
            $header_data = true;
            $contents .= implode($header, ",")."\n";
            $contents .= implode($header_rei, ",")."\n";
        } elseif ($this->field["OUTPUT"]=="3") {
            // エラー出力
            $contents .= implode($header_err, ",")."\n";
        } elseif ($this->field["OUTPUT"]=="4") {
            // データ出力
            if ($this->field["HEADER"]=="on") {
                $contents .= implode($header, ",")."\n";
            }
        }

        //ヘッダ以外
        if ($this->field["OUTPUT"] != "1") {
            $db = Query::dbCheckOut();

            if ($this->field["OUTPUT"]=="3") {
                //エラー
                $query = knjl015cQuery::selectCsvErrQuery();
            } elseif ($this->field["OUTPUT"]=="4") {
                //データ
                $query = knjl015cQuery::selectMainQuery($this);
            }

            //SQL文発行
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                if ($this->field["OUTPUT"]=="4") {                //データ
                    $row["BIRTHDAY"] = str_replace("-", "/", $row["BIRTHDAY"]);

                    if ($row["FS_Y"] && $row["FS_M"]) {
                        $fs_seirekiY = ((int)$row["S_YEAR"]) + ((int)$row["FS_Y"]) - 1;
                        $row["FS_YM"] = $fs_seirekiY."/".$row["FS_M"];
                    }

                    $row = str_replace("\r\n", "", str_replace(",", ".", $row));   //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。

                    $sep = "";
                    foreach ($header as $key => $val) {
                        $contents .= $sep.$row[$key];
                        $sep = ",";
                    }
                    $contents .= "\n";
                } else {
                    $contents .= implode(",", $row) ."\n";
                }
                $header_data = true;
            }

            $result->free();
            Query::dbCheckIn($db);
        }

        //ＣＳＶ出力
        if ($header_data) {
            if ($this->field["OUTPUT"]=="3") {        //エラー
                $couse = "エラー出力";
            } else {                                //エラー以外
                $couse = "志願者基礎データ";
            }
            $filename = sprintf("%s.csv", $couse);
            common::downloadContents($contents, $filename);
            return true;
        } else {
            $this->warning = "該当データは、ありません。";
            return false;
        }
    }
}
