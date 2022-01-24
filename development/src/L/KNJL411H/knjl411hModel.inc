<?php

class knjl411hModel extends Model
{
    public $schoolKind;
    public $cmd;
    public $field = array();
    public $file;

    /**
     * メンバ変数の初期化
     */
    public function knjl411hModel()
    {
    }

    public function init()
    {
        if (VARS::request("PROGRAMID")) {
            //$this->schoolKind = VARS::get("URL_SCHOOLKIND");
            $this->field = array();
            common::GetControlMaster_Fnc($this->control);
        }

        $this->cmd = VARS::request("cmd");
        $this->examyear = CTRL_YEAR + 1;

        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        //入試制度
        if (VARS::post("APPLICANTDIV")) {
            $this->field["APPLICANTDIV"] = VARS::post("APPLICANTDIV");
        }
        if (VARS::post("OUTPUT")) {
            $this->field["OUTPUT"] = VARS::post("OUTPUT");
        }
        if (VARS::post("SHORI_MEI")) {
            $this->field["SHORI_MEI"] = VARS::post("SHORI_MEI");
        }
        if (VARS::post("HEADER")) {
            $this->field["HEADER"] = VARS::post("HEADER");
        }
        if (VARS::post("HR_CLASS")) {
            $this->field["HR_CLASS"] = VARS::post("HR_CLASS");
        }

        $this->file = VARS::file("FILE");


        //CSVヘッダ項目
        $this->header = array();
        $this->header["ENTEXAMYEAR"]              = "年度"; //必須
        $this->header["APPLICANTDIV_NAME"]        = "入試区分"; //必須
        $this->header["LOGIN_SEQ"]                = "ログインユーザSEQ"; //必須
        $this->header["LOGIN_MAIL"]               = "ログインメールアドレス"; //必須
        $this->header["APP_MAIL"]                 = "申込時メールアドレス"; //必須
        $this->header["SHIGANSYA_SEQ"]            = "志願者SEQ"; //必須
        $this->header["NAME_SEI"]                 = "志願者氏名（漢字）（姓）"; //必須
        $this->header["NAME_MEI"]                 = "志願者氏名（漢字）（名）"; //必須
        $this->header["NAME_KANA_SEI"]            = "志願者氏名（カナ）（姓）"; //必須
        $this->header["NAME_KANA_MEI"]            = "志願者氏名（カナ）（名）"; //必須
        $this->header["SEX_NAME"]                 = "性別";
        $this->header["NO_HAIHUN_BIRTHDAY"]       = "生年月日";  //必須
        $this->header["NO_HAIHUN_ZIPCD"]          = "郵便番号";  //必須
        $this->header["PREF_NAME"]                = "都道府県";  //必須
        $this->header["CITY_NAME"]                = "市区町村";  //必須
        $this->header["BANCHI_NAME"]              = "町名・番地";  //必須
        $this->header["ADDRESS2"]                 = "建物名・部屋番号";
        $this->header["TELNO"]                    = "電話番号";
        $this->header["FS_CD"]                    = ($this->field["APPLICANTDIV"] == "2") ? "出身校コード" : "出身小学校コード";
        $this->header["FS_NAME"]                  = ($this->field["APPLICANTDIV"] == "2") ? "出身校名称" : "出身小学校名称";
        $this->header["FS_NAME_OTHER"]            = ($this->field["APPLICANTDIV"] == "2") ? "出身校名称その他" : "出身小学校名称その他";
        if ($this->field["APPLICANTDIV"] == "1") {
            $this->header["PS_CD"]                = "塾（１）コード";
            $this->header["PS_NAME"]              = "塾（１）名称";
            $this->header["PS_NAME_OTHER"]        = "塾（１）名称その他";
            $this->header["PS_NAME2"]             = "塾（２）";
        }
        $this->header["GNAME_SEI"]                = "保護者氏名(漢字)（姓）";  //必須
        $this->header["GNAME_MEI"]                = "保護者氏名(漢字)（名）";  //必須
        $this->header["GKANA_SEI"]                = "保護者氏名(カナ)（姓）";  //必須
        $this->header["GKANA_MEI"]                = "保護者氏名(カナ)（名）";  //必須
        $this->header["NO_HAIHUN_GZIPCD"]         = "郵便番号";  //必須
        $this->header["GPREF_NAME"]               = "都道府県";  //必須
        $this->header["GCITY_NAME"]               = "市区町村";  //必須
        $this->header["GBANCHI_NAME"]             = "町名・番地";  //必須
        $this->header["GADDRESS2"]                = "建物名・部屋番号";
        $this->header["EMERGENCYTELNO"]           = ($this->field["APPLICANTDIV"] == "2") ? "緊急連絡先" : "連絡先";
        $this->header["EMERGENCYCALL"]         = ($this->field["APPLICANTDIV"] == "2") ? "上記緊急連絡先の名称（例：父の携帯番号）" : "連絡先番号詳細";
        $this->header["RELATIONSHIP"]             = ($this->field["APPLICANTDIV"] == "2") ? "※本人との続柄" : "志願者との関係";
        if ($this->field["APPLICANTDIV"] == "2") {
            $this->header["RELATIONSHIP_OTHER"]   = "本人との続柄(その他)";
        }
        $this->header["TESTDIV1_NAME"]            = "1回目試験";
        $this->header["TESTDIV1_EXAMNO"]          = "受験番号";
        $this->header["TESTDIV2_NAME"]            = "2回目試験";
        $this->header["TESTDIV2_EXAMNO"]          = "受験番号";
        if ($this->field["APPLICANTDIV"] == "1") {
            $this->header["TESTDIV3_NAME"]        = "3回目試験";
            $this->header["TESTDIV3_EXAMNO"]      = "受験番号";
            $this->header["TESTDIV4_NAME"]        = "4回目試験";
            $this->header["TESTDIV4_EXAMNO"]      = "受験番号";
            $this->header["TESTDIV5_NAME"]        = "5回目試験";
            $this->header["TESTDIV5_EXAMNO"]      = "受験番号";
        }

        //CSV処理で使用するデータを事前に取得
        $this->getMstInfo();
    }

    public function getMstInfo()
    {
        $db = Query::dbCheckOut();

        //元号取得
        $this->nameMstL007 = array();
        $query = knjl411hQuery::getNameMst($this, "L007");
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $this->nameMstL007[$row["NAMECD2"]] = array("ERA_SYEAR" => $row["NAMESPARE1"],
                                                        "ERA_SDATE" => $row["NAMESPARE2"],
                                                        "ERA_EDATE" => $row["NAMESPARE3"],
                                                 );
        }

        //続柄取得
        $this->nameMstH201 = array();
        $query = knjl411hQuery::getNameMst($this, "H201");
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $this->nameMstH201[$row["NAME1"]]  = $row["NAMECD2"];
        }

        //受験型取得
        $this->examTypeMst = array();
        $query = knjl411hQuery::getExamTypeMst($this);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $this->examTypeMst[$row["MAPPING_NAME"]]  = $row["EXAM_TYPE"];
        }

        //受験コースマスタ取得
        $this->examCourseMst = array();
        $query = knjl411hQuery::getExamCourseMst($this);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $this->examCourseMst[$row["MAPPING_NAME"]]  = $row["EXAMCOURSECD"];
        }

        Query::dbCheckIn($db);
    }

    //入力項目チェック
    public function validateInput()
    {
        return true;
    }


    /**
     * CSV取込処理
    */
    public function &getExecModel()
    {
        //ファイルの文字列の取得
        $this->data_arr = array();
        $this->data_arr = $this->GetFileString();

        //データの整合性チェック
        if ($this->IsDataMatched($this->data_arr)) {
            if ($this->field["SHORI_MEI"] == "1") {
                // 更新
                $cnt = knjl411hQuery::updateQueryCsv($this, $this->data_arr);
            } else {
                // 削除
                $cnt = knjl411hQuery::deleteQueryCsv($this, $this->data_arr);
            }

            $db = Query::dbCheckOut();
            $errcnt = $db->getOne("SELECT COUNT(*) FROM W_CSVMSG_PRG_DAT WHERE PROGRAMID = '".PROGRAMID."' ");
            Query::dbCheckIn($db);

            $this->setMessage(MSG202, "処理済件数　：".$cnt."件\\nエラー件数    ：".$errcnt."件");
        }
    }
    /**
     * CSV出力処理
    */
    public function &getDownloadModel()
    {
        if ($this->field["OUTPUT"] == "3") {
            //エラー出力
            return $this->getErrorCsvDownloadModel();
        }
        $this->setWarning("MSG901", "処理区分");
        return false;
    }

    //取込
    public function GetFileString()
    {
        $temp_arr = array();
        if (!is_uploaded_file($this->file["tmp_name"])) {        //HTTP POSTによりアップロードされたファイルかどうかを調べる
            $this->warning = "ファイル名が不正です。";
        } else {
            $checkdata = array();
            $checkdata["ENTEXAMYEAR"]              = array("HISSU",   4, "年度");
            $checkdata["APPLICANTDIV_NAME"]        = array("HISSU", 999, "入試区分");
            $checkdata["LOGIN_SEQ"]                = array("HISSU",   5, "ログインユーザSEQ");
            $checkdata["LOGIN_MAIL"]               = array("HISSU", 150, "ログインメールアドレス");
            $checkdata["APP_MAIL"]                 = array("HISSU", 150, "申込時メールアドレス");
            $checkdata["SHIGANSYA_SEQ"]            = array("HISSU",   5, "志願者SEQ");
            $checkdata["NAME"]                     = array("HISSU", 120, "志願者氏名（漢字）");
            $checkdata["NAME_KANA"]                = array("HISSU", 240, "志願者氏名（カナ）");
            $checkdata["SEX"]                      = array("",        1, "性別");
            $checkdata["BIRTHDAY"]                 = array("",       11, "生年月日");
            $checkdata["ZIPCD"]                    = array("HISSU",   8, "郵便番号");
            $checkdata["ADDRESS1"]                 = array("HISSU", 300, "都道府県/市区町村/町名・番地");
            $checkdata["ADDRESS2"]                 = array("",      300, "建物名・部屋番号");
            $checkdata["TELNO"]                    = array("",       14, "電話番号");
            $checkdata["FS_CD"]                    = array("HISSU",  12, $this->header["FS_CD"]); //中高で名称が変わるのでヘッダで設定したものを入れている
            if ($this->field["APPLICANTDIV"] == "1") {
                $checkdata["PS_CD"]          =  array("",   150, "塾（１）コード");
                $checkdata["PS_NAME"]        =  array("",   150, "塾（１）名称");
                $checkdata["PS_NAME_OTHER"]  =  array("",   150, "塾（１）名称その他");
            }
            $checkdata["GNAME"]                    = array("HISSU", 120, "保護者氏名(漢字)");
            $checkdata["GKANA"]                    = array("HISSU", 240, "保護者氏名(カナ)");
            $checkdata["GZIPCD"]                   = array("HISSU",   8, "保護者住所 郵便番号");
            $checkdata["GADDRESS1"]                = array("HISSU", 300, "保護者住所 都道府県/市区町村/町名・番地");
            $checkdata["GADDRESS2"]                = array("",      300, "保護者住所 建物名・部屋番");
            $checkdata["EMERGENCYTELNO"]           = array("",       14, "連絡先");
            $checkdata["EMERGENCYCALL"]         = array("",      150, "連絡先番号詳細");
            $checkdata["GRELATIONSHIP"]            = array("HISSU",   2, "続柄");
            $checkdata["TESTDIV1_EXAMNO"]          = array("EXAMNO", 10, "受験番号(1回目試験)");
            $checkdata["TESTDIV2_EXAMNO"]          = array("EXAMNO", 10, "受験番号(2回目試験)");
            if ($this->field["APPLICANTDIV"] == "1") {
                $checkdata["TESTDIV3_EXAMNO"]      = array("EXAMNO", 10, "受験番号(3回目試験)");
                $checkdata["TESTDIV4_EXAMNO"]      = array("EXAMNO", 10, "受験番号(4回目試験)");
                $checkdata["TESTDIV5_EXAMNO"]      = array("EXAMNO", 10, "受験番号(5回目試験)");
            }
    
            $db = Query::dbCheckOut();
            $db->autoCommit(false);

            //エラーデータの削除
            $query = knjl411hQuery::deleteCsvMsgPrgDat();
            $db->query($query);

            $fp = fopen($this->file["tmp_name"], "r");
            $i  = 0;
            $line = 1;  //行数
            $hasHeader = false;
            while (!feof($fp)) {
                $buffer = fgets($fp, 4096);
                if (trim($buffer) != '') {
                    if (!$hasHeader && $this->field["HEADER"] == "on") {
                        $hasHeader = true;
                        $line++;
                        continue;
                    }
                    //文字コードを判別、改行コードを削除
                    $buffer = trim(i18n_convert($buffer, i18n_http_output(), "SJIS-win"));
                    $tmp    = common::csv2array($buffer);
                    $tmp    = str_replace("↓", "\n", $tmp);

                    //CSV取込
                    $tmpNo = 0;
                    $data = array();
                    foreach ($this->header as $key => $val) {
                        $data[$key] = $tmp[$tmpNo];
                        $tmpNo++;
                    }

                    /*データ前処理*/
                    $this->preprocess($data);

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

    //年齢計算(※入力日付の形式はYYYYMMDD)
    public function getCalcAge($baseDate, $targetDate)
    {
        return floor(($targetDate - $baseDate) / 10000);
    }

    //日付変換(YYYYMMDD => YYYY-MM-DD)
    public function getFormattedDate($noFormattedDate)
    {
        $y = substr($noFormattedDate, 0, 4);
        $m = substr($noFormattedDate, 4, 2);
        $d = substr($noFormattedDate, 6, 2);
        return "{$y}-{$m}-{$d}";
    }

    //郵便番号整形
    public function getFormattedZipCd($noFormattedZipCd)
    {
        return substr($noFormattedZipCd, 0, 3)."-".substr($noFormattedZipCd, 3, 4);
    }


    //和暦情報取得(※入力日付の形式はYYYY-MM-DD)
    public function getEraInfo($targetDate, $nameMstL007)
    {
        $eraCd = "";
        $eraYear = "";
        foreach ($nameMstL007 as $nameCd2 => $eraInfo) {
            if ($eraInfo["ERA_SDATE"] <= $targetDate && $targetDate <= $eraInfo["ERA_EDATE"]) {
                $eraCd = $nameCd2;
                $y = substr($targetDate, 0, 4);
                $m = substr($targetDate, 5, 2);
                $d = substr($targetDate, 8, 2);
                $tmp = ($y + 1) - $eraInfo["ERA_SYEAR"];
                $eraYear = sprintf("%02d", $tmp);
                $eraMonth = $m;
                $eraDay = $d;
            }
        }
        return array($eraCd, $eraYear, $eraMonth, $eraDay);
    }

    /*データ前処理*/
    public function preprocess(&$data)
    {
        //受験番号
        $data["EXAMNO"] = $data["SHIGANSYA_SEQ"];

        //日付整形(誕生日)
        $data["BIRTHDAY"] = $this->getFormattedDate($data["NO_HAIHUN_BIRTHDAY"]);
        list($eraCd, $eraYear, $eraMonth, $eraDay) = $this->getEraInfo($data["BIRTHDAY"], $this->nameMstL007);
        $data["ERACD"]    = $eraCd;
        $data["BIRTH_Y"]  = $eraYear;
        $data["BIRTH_M"]  = $eraMonth;
        $data["BIRTH_D"]  = $eraDay;

        //日付整形(卒業日付)
        $data["FS_DAY"]     = "{$this->examyear}-03-01";
        $data["FS_GRDYEAR"] = $this->examyear;
        list($eraCd, $eraYear, $eraMonth, $eraDay) = $this->getEraInfo($data["FS_DAY"], $this->nameMstL007);
        $data["FS_ERACD"]   = $eraCd;
        $data["FS_Y"]       = $eraYear;
        $data["FS_M"]       = $eraMonth;

        //名前連結
        $data["NAME"]       = $data["NAME_SEI"]."　".$data["NAME_MEI"];
        $data["NAME_KANA"]  = $data["NAME_KANA_SEI"]."　".$data["NAME_KANA_MEI"];
        $data["GNAME"]      = $data["GNAME_SEI"]."　".$data["GNAME_MEI"];
        $data["GKANA"]      = $data["GKANA_SEI"]."　".$data["GKANA_MEI"];
        
        //性別コード変換
        $data["SEX"] = ($data["SEX_NAME"] == "男") ? 1 : (($data["SEX_NAME"] == "女") ? 2 : "");

        //年齢から値を算出
        $age = $this->getCalcAge($data["NO_HAIHUN_BIRTHDAY"], "{$this->examyear}0401");
        $data["FS_GRDDIV"]  = ($this->field["APPLICANTDIV"] == "1" || $age < 16) ? "1" : "2"; //卒業区分取得
        $data["ROUNIN_FLG"] = ($age < 16) ? "" : "1";  //浪人生フラグ

        //郵便番号整形
        $data["ZIPCD"]  = (trim($data["NO_HAIHUN_ZIPCD"]) != "") ? $this->getFormattedZipCd($data["NO_HAIHUN_ZIPCD"]) : "";
        $data["GZIPCD"] = (trim($data["NO_HAIHUN_GZIPCD"]) != "") ? $this->getFormattedZipCd($data["NO_HAIHUN_GZIPCD"]) : "";

        //住所連結
        $data["ADDRESS1"]  = $data["PREF_NAME"].$data["CITY_NAME"].$data["BANCHI_NAME"];
        $data["GADDRESS1"] = $data["GPREF_NAME"].$data["GCITY_NAME"].$data["GBANCHI_NAME"];
        
        //続柄コード取得
        $data["GRELATIONSHIP"]  = $this->nameMstH201[$data["RELATIONSHIP"]];

        //各回試験文字列からデータ抽出
        $testDivCnt = ($this->field["APPLICANTDIV"] == "1") ? 5 : 2;
        $data["RECEPT_DATA"] = array();
        for ($testDiv = 1; $testDiv <= $testDivCnt; $testDiv++) {
            $receptNo = $data["TESTDIV{$testDiv}_EXAMNO"];
            $testDivName = $data["TESTDIV{$testDiv}_NAME"];
            //「n回目試験」列の文字列から「受験型・出願区分・希望コース」を取得
            if ($this->field["APPLICANTDIV"] == "1") {
                if (preg_match("/(.*?)：.*1：(.*?) 2：(.*)/", $testDivName, $match)) {
                    $examCourses = array();
                    for ($i = 1; $i <= 2; $i++) {
                        $course = $this->examCourseMst[$match[$i + 1]];
                        $data["HOPE_COURSE_{$i}"] = $course;
                        $data["SHDIV_{$i}"] = ($course != "") ? "2" : "";
                    }
                    $examType = $this->examTypeMst[$match[1]]; //最初のセミコロンまでの英字がマッチすればOK
                }
            } else {
                if (preg_match("/(.*1:(.*?))\/2:(.*?)\/3:(.*)/", $testDivName, $match)) {
                    for ($i = 1; $i <= 3; $i++) {
                        if ($match[$i + 1] == "特A特別選抜") {
                            $shdiv = "1";
                        } else {
                            if (strpos($match[$i + 1], "推")) {
                                $shdiv = "1";
                                $match[$i + 1] = str_replace("推", "", $match[$i + 1]);
                            } elseif (strpos($match[$i + 1], "専")) {
                                $shdiv = "2";
                                $match[$i + 1] = str_replace("専", "", $match[$i + 1]);
                            } elseif (strpos($match[$i + 1], "併")) {
                                $shdiv = "3";
                                $match[$i + 1] = str_replace("併", "", $match[$i + 1]);
                            }
                        }
                        $course = $this->examCourseMst[$match[$i + 1]];
                        $data["HOPE_COURSE_{$i}"] = $course;
                        $data["SHDIV_{$i}"] = ($course != "") ? $shdiv : "";
                    }
                    $examType = $this->examTypeMst[preg_replace("/(推|専|併)/", "", $match[1])]; //最初のスラッシュまでの文字列が認識用文字列とマッチすればOK
                }
            }

            if (strlen(trim($receptNo)) && $receptNo != "未発番") {
                $data["RECEPT_DATA"][$testDiv] = array("RECEPTNO"      => $receptNo,
                                                       "EXAMNO"        => $data["EXAMNO"],
                                                       "EXAM_TYPE"     => $examType,
                                                       "HOPE_COURSE_1" => $data["HOPE_COURSE_1"],
                                                       "SHDIV_1"       => $data["SHDIV_1"],
                                                       "HOPE_COURSE_2" => $data["HOPE_COURSE_2"],
                                                       "SHDIV_2"       => $data["SHDIV_2"],
                                                       "HOPE_COURSE_3" => $data["HOPE_COURSE_3"],
                                                       "SHDIV_3"       => $data["SHDIV_3"],
                                                       );
            }
        }
    }

    //エラーチェック
    public function errCheck($db, $data, $data_each, $line)
    {
        $returnFlg       = true;
        $hissuErrFlg     = false;    //必須エラーフラグ
        $numericErrFlg   = false;    //数字エラーフラグ
        $lengthErrFlg    = false;    //レングスエラーフラグ
        $existErrFlg     = false;    //存在エラーフラグ
        $orderErrFlg     = false;    //その他エラーフラグ

        $hissuErrMsg     = "";       //必須エラーメッセージ
        $numericErrMsg   = "";       //数字エラーメッセージ
        $lengthErrMsg    = "";       //レングスエラーメッセージ
        $existErrMsg     = "";       //存在エラーメッセージ
        $orderErrMsg     = "";       //その他エラーメッセージ
        $hissuSep        = "";
        $numericSep      = "";
        $lengthSep       = "";
        $existSep        = "";
        $orderSep        = "";

        foreach ($data_each as $key => $val) {
            list($div, $size, $label) = $val;

            //必須項目チェック
            if ($div == "HISSU") {
                if (!strlen(trim($data[$key]))) {
                    $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $label);
                }
            }
            //数値チェック
            if (in_array($key, array("ENTEXAMYEAR", "APPLICANTDIV", "LOGIN_SEQ", "SHIGANSYA_SEQ")) && $data[$key] != "") {
                if (!is_numeric($data[$key])) {
                    $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, $label);
                }
            }
            //文字数チェック
            if (strlen($data[$key]) > $size) {
                $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $label);
            }

            //入試年度チェック
            if (in_array($key, array("ENTEXAMYEAR")) && $data[$key] != "") {
                if ((CTRL_YEAR + 1) != $data[$key]) {
                    $returnFlg = $this->errSet($orderErrFlg, $orderErrMsg, "※その他:", $orderSep, $label);
                }
            }
        }

        if (!$returnFlg) {
            $check_error = $hissuErrMsg.$numericErrMsg.$lengthErrMsg.$existErrMsg.$orderErrMsg;
            if (strlen($check_error) > 120) {
                $check_error = mb_strcut($check_error, 0, 115);
                $check_error .= "..他";
            }
            $this->err_data[$line]["MSG"] = $check_error;
            knjl411hQuery::insertCsvMsgPrgDat($db, $line, $check_error);
        }
        return $returnFlg;
    }

    //エラーメッセージをセット
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

    //データの整合性チェック
    public function IsDataMatched(&$data_arr)
    {
        //５千件を超えたら制限エラー
        if (count($data_arr) > 5000) {
            $this->setWarning("MSG915", "処理件数を５千件以下にして下さい 処理件数：".count($data_arr)."件");
            return false;
        }
        return true;
    }

    /*================================== エラーデータ処理 ==================================*/
    //エラー出力
    public function getErrorCsvDownloadModel()
    {
        //項目
        $header = array(
              "MSGROW"    => "行番号"
            , "MSGREMARK" => "メッセージ"
        );

        $contents  = "";
        if ($this->field["HEADER"] == "on") {
            $contents .= implode($header, ",")."\n";
        }

        //DB接続
        $db = Query::dbCheckOut();

        $isData = false;
        //データ取得
        $query = knjl411hQuery::selectCsvMsgPrgDat($this);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $data = array();
            // $row["DUMMY"] = "DUMMY";
            foreach ($header as $key => $val) {
                $data[] = $sep.$row[$key];
            }
            $contents .= implode($data, ",") ."\n";
            $isData = true;
        }
        $result->free();
        Query::dbCheckIn($db);

        //ＣＳＶ出力
        if ($isData) {
            $fileName = "エラー出力.csv";
            common::downloadContents($contents, $fileName, "SJIS-win");
            return true;
        } else {
            $this->warning = "該当データは、ありません。";
            return false;
        }
    }
}
