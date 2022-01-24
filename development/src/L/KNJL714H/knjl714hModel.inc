<?php
class knjl714hModel extends Model
{
    public $field  = array();
    public $data   = array();
    public $cmd;
    public $file;
    public $ObjYear;

    public function init()
    {
        $this->ObjYear = CTRL_YEAR+1;

        $this->field = array("APPLICANTDIV"     =>  VARS::post("APPLICANTDIV"),     //学校種別(データ取込)
                             "TESTDIV1"         =>  VARS::post("TESTDIV1"),          //入試区分1(データ取込)
                             "TESTDIV2"         =>  VARS::post("TESTDIV2"),          //入試区分2(データ取込)
                             "KINDDIV"          =>  VARS::post("KINDDIV"),          //入試種別(データ取込)
                             "EXAM_TYPE"        =>  "1",                            //固定
                             "OUTPUT"           =>  VARS::post("OUTPUT"),           //出力取込種別
                             "HEADER"           =>  VARS::post("HEADER"),           //ヘッダ有
                             "SHORI_MEI"        =>  VARS::post("SHORI_MEI")         //処理名
                             );

        $this->cmd = VARS::request("cmd");

        if (VARS::file("FILE")) {
            $this->field["FILE"] = VARS::file("FILE");      //ファイルのアップロード
            $this->field["FILE"]["name"] = mb_convert_encoding($this->field["FILE"]["name"], "SJIS-win", "UTF-8");
        }

        //プロパティファイルの読込み
        $this->getPropertiesAll();
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
            if ($this->field["SHORI_MEI"] == "1") {
                // 更新
                $cnt = knjl714hQuery::updateQueryCsv($this, $this->data_arr);
            } else {
                // 削除
                $cnt = knjl714hQuery::deleteQueryCsv($this, $this->data_arr);
            }

            $db = Query::dbCheckOut();
            $errcnt = $db->getOne("SELECT COUNT(*) FROM W_CSVMSG_PRG_DAT WHERE PROGRAMID = '".PROGRAMID."' ");
            Query::dbCheckIn($db);

            $this->setMessage(MSG202, "処理済件数　：".$cnt."件\\nエラー件数    ：".$errcnt."件");
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
            $checkdata = array();

            //チェック用 CSV取込項目
            $checkdata = array();
            $checkdata["ENTEXAMYEAR"]              = array(4,    "HISSU",  "INT",   "年度");
            $checkdata["LOGIN_USER_SEQ"]           = array(10,   "HISSU",  "INT",   "ログインユーザSEQ");
            $checkdata["NAME_SEI"]                 = array(60,   "HISSU",  "",      "志願者氏名（漢字）（姓）");
            $checkdata["NAME_MEI"]                 = array(60,   "HISSU",  "",      "志願者氏名（漢字）（名）");
            $checkdata["NAME_KANA_SEI"]            = array(120,  "",       "",      "志願者氏名（カナ）（姓）");
            $checkdata["NAME_KANA_MEI"]            = array(120,  "",       "",      "志願者氏名（カナ）（名）");
            $checkdata["SEX"]                      = array(10,   "HISSU",  "",      "性別");
            $checkdata["BIRTHDAY"]                 = array(10,   "HISSU",  "DATE",  "生年月日");
            $checkdata["ZIPCD"]                    = array(8,    "",       "",      "郵便番号");
            $checkdata["PREF_NAME"]                = array(30,   "",       "",      "都道府県");
            $checkdata["CITY_NAME"]                = array(90,   "",       "",      "市区町村");
            $checkdata["BANCHI_NAME"]              = array(90,   "",       "",      "町名・番地");
            $checkdata["ADDRESS2"]                 = array(150,  "",       "",      "建物名・部屋番号");
            $checkdata["TELNO"]                    = array(14,   "",       "",      "電話番号");
            $checkdata["MIRAI_FS_CD"]              = array(10,   "",       "",      "出身校コード");
            if ($this->field["APPLICANTDIV"] == "2" && $this->field["TESTDIV1"] == 1) {
                $checkdata["QUALIFIED_ENG_NAME"]        = array(300,   "",       "",      "【資格検定】英語検定");
                $checkdata["QUALIFIED_KANJI_NAME"]      = array(300,   "",       "",      "【資格検定】漢字検定");
                $checkdata["QUALIFIED_MATH_NAME"]       = array(300,   "",       "",      "【資格検定】実用数学技能検定");
                $checkdata["QUALIFIED_READING_NAME"]    = array(300,   "",       "",      "【資格検定】語彙・読解力検定");
                $checkdata["ATHLETIC_PRIZE_NAME"]       = array(300,   "",       "",      "【運動関係】賞状・プログラム等");
                $checkdata["CULTURE_PRIZE_NAME"]        = array(300,   "",       "",      "【文化関係】賞状・プログラム等");
                $checkdata["SCH_COUNCIL_PRIZE_NAME"]    = array(300,   "",       "",      "【生徒会活動関係】");
            } else {
            }
            $checkdata["GNAME_SEI"]                = array(60,   "",       "",      "保護者氏名(漢字)（姓）");
            $checkdata["GNAME_MEI"]                = array(60,   "",       "",      "保護者氏名(漢字)（名）");
            $checkdata["GKANA_SEI"]                = array(120,  "",       "",      "保護者氏名(カナ)（姓）");
            $checkdata["GKANA_MEI"]                = array(120,  "",       "",      "保護者氏名(カナ)（名）");
            $checkdata["TEST_DATE1"]               = array(60,   "",       "",      "試験日");
            $checkdata["EXAMNO1"]                  = array(10,   "",       "",      "受験番号");
            if ($this->field["APPLICANTDIV"] == "1") {
                $checkdata["TEST_DATE2"]           = array(60,   "",       "",      "試験日");
                $checkdata["EXAMNO2"]              = array(10,   "",       "",      "受験番号");
            }

            //エラーデータの削除
            $query = knjl714hQuery::deleteQueryErr();
            $result = $db->query($query);

            $fp = fopen($this->field["FILE"]["tmp_name"], "r");
            $i  = 0;
            $line = 1;  //行数
            $header = false;

            while (!feof($fp)) {
                $tmp = fgetcsv($fp, 4096);

                if ($tmp) {
                    //文字コードを判別、改行コードを削除
                    $buffer = implode(",", $tmp);
                    $buffer = trim(i18n_convert($buffer, i18n_http_output(), "SJIS-win"));
                    $buffer = str_replace("\"", "", $buffer);
                    $buffer = str_replace("↓", "\n", $buffer);
                    $tmp = explode(",", $buffer);

                    //配列の要素数チェック
                    $checkcount = count($tmp);
                    //ホワイトスペースを取り除く
                    for ($t = 0; $t < $checkcount; $t++) {
                        $tmp[$t] = trim($tmp[$t]);
                    }

                    if (!$header && $this->field["HEADER"] == "on") {
                        $header = true;
                        $line++;

                        continue;
                    }

                    //チェック用 CSV取込項目
                    $cnt = 0;
                    $data = array();
                    $data["ENTEXAMYEAR"]              = $tmp[$cnt++];
                    $data["APPLICANTDIV"]             = $tmp[$cnt++];
                    $data["APPNO"]                    = $tmp[$cnt++];
                    $data["LOGIN_USER_SEQ"]           = $tmp[$cnt++];
                    $data["LOGIN_MAIL"]               = $tmp[$cnt++];
                    $data["APP_MAIL"]                 = $tmp[$cnt++];
                    $data["SHIGANSYA_SEQ"]            = $tmp[$cnt++];
                    $data["NAME_SEI"]                 = $tmp[$cnt++];
                    $data["NAME_MEI"]                 = $tmp[$cnt++];
                    $data["NAME_KANA_SEI"]            = $tmp[$cnt++];
                    $data["NAME_KANA_MEI"]            = $tmp[$cnt++];
                    $data["SEX"]                      = $tmp[$cnt++];
                    $data["BIRTHDAY"]                 = $tmp[$cnt++];
                    $data["ZIPCD"]                    = $tmp[$cnt++];
                    $data["PREF_NAME"]                = $tmp[$cnt++];
                    $data["CITY_NAME"]                = $tmp[$cnt++];
                    $data["BANCHI_NAME"]              = $tmp[$cnt++];
                    $data["ADDRESS2"]                 = $tmp[$cnt++];
                    $data["TELNO"]                    = $tmp[$cnt++];
                    $data["MIRAI_FS_CD"]              = $tmp[$cnt++];
                    $data["MIRAI_FS_NAME"]            = $tmp[$cnt++];
                    $data["MIRAI_FS_NAME_OTHER"]      = $tmp[$cnt++];
                    if ($this->field["APPLICANTDIV"] == "2" && $this->field["TESTDIV1"] == 1) {
                    } else {
                        $data["MIRAI_SH_CD1"]             = $tmp[$cnt++];
                        $data["MIRAI_SH_NAME1"]           = $tmp[$cnt++];
                        $data["MIRAI_SH_NAME_OTHER1"]     = $tmp[$cnt++];
                        $data["MIRAI_SH_CD2"]             = $tmp[$cnt++];
                        $data["MIRAI_SH_NAME2"]           = $tmp[$cnt++];
                        $data["MIRAI_SH_NAME_OTHER2"]     = $tmp[$cnt++];
                        $data["MIRAI_SH_CD3"]             = $tmp[$cnt++];
                        $data["MIRAI_SH_NAME3"]           = $tmp[$cnt++];
                        $data["MIRAI_SH_NAME_OTHER3"]     = $tmp[$cnt++];
                    }
                    $data["MIRAI_PS_CD"]              = $tmp[$cnt++];
                    $data["MIRAI_PS_NAME"]            = $tmp[$cnt++];
                    $data["MIRAI_PS_NAME_OTHER"]      = $tmp[$cnt++];
                    if ($this->field["APPLICANTDIV"] == "2" && $this->field["TESTDIV1"] == 1) {
                        $data["QUALIFIED_ENG_NAME"]        = $tmp[$cnt++];
                        $data["QUALIFIED_KANJI_NAME"]      = $tmp[$cnt++];
                        $data["QUALIFIED_MATH_NAME"]       = $tmp[$cnt++];
                        $data["QUALIFIED_READING_NAME"]    = $tmp[$cnt++];
                        $data["ATHLETIC_PRIZE_NAME"]       = $tmp[$cnt++];
                        $data["CULTURE_PRIZE_NAME"]        = $tmp[$cnt++];
                        $data["SCH_COUNCIL_PRIZE_NAME"]    = $tmp[$cnt++];
                        $data["NOTE"]                 = $tmp[$cnt++];
                    } else {
                        $data["EXTRA_ACTIVITYS"]      = $tmp[$cnt++];
                    }
                    $data["GNAME_SEI"]                = $tmp[$cnt++];
                    $data["GNAME_MEI"]                = $tmp[$cnt++];
                    $data["GKANA_SEI"]                = $tmp[$cnt++];
                    $data["GKANA_MEI"]                = $tmp[$cnt++];
                    $data["GZIPCD"]                   = $tmp[$cnt++];
                    $data["GPREF_NAME"]               = $tmp[$cnt++];
                    $data["GCITY_NAME"]               = $tmp[$cnt++];
                    $data["GBANCHI_NAME"]             = $tmp[$cnt++];
                    $data["GADDRESS2"]                = $tmp[$cnt++];
                    $data["GTELNO"]                   = $tmp[$cnt++];
                    $data["EXPLAIN"]                  = $tmp[$cnt++];
                    $data["RELATIONSHIP"]             = $tmp[$cnt++];
                    $data["TONAI_CONTACT_ZIPCD"]      = $tmp[$cnt++];
                    $data["TONAI_CONTACT_ADDRESS"]    = $tmp[$cnt++];
                    $data["TONAI_CONTACT_NAME"]       = $tmp[$cnt++];
                    $data["TONAI_CONTACT_TELNO"]      = $tmp[$cnt++];
                    $data["APP_INFO_DIV"]             = $tmp[$cnt++];
                    $data["SRV_RCPT_DIV"]             = $tmp[$cnt++];
                    $data["SRV_RCPT_MAIL_DATE"]       = $tmp[$cnt++];
                    $data["AGREE_FLG"]                = $tmp[$cnt++];
                    $data["COUNTER_RCPTNO"]           = $tmp[$cnt++];
                    $data["REMARKS"]                  = $tmp[$cnt++];
                    $data["SETTLE_MONEY"]             = $tmp[$cnt++];
                    $data["EXAM_MONEY"]               = $tmp[$cnt++];
                    $data["FEES"]                     = $tmp[$cnt++];
                    $data["PAY_TYPE_CD"]              = $tmp[$cnt++];
                    $data["PAY_DUE_DATE"]             = $tmp[$cnt++];
                    $data["PAY_DAY"]                  = $tmp[$cnt++];
                    $data["PAY_DAY_MANUAL"]           = $tmp[$cnt++];
                    $data["PAY_INFO_DIV"]             = $tmp[$cnt++];
                    $data["APP_COMP_DAY"]             = $tmp[$cnt++];
                    $data["CANCEL_FLG"]               = $tmp[$cnt++];
                    $data["CANCEL_DATE"]              = $tmp[$cnt++];
                    $data["TEST_DATE1"]               = $tmp[$cnt++];
                    $data["EXAMNO1"]                  = $tmp[$cnt++];
                    if ($this->field["APPLICANTDIV"] == "1") {
                        $data["TEST_DATE2"]           = $tmp[$cnt++];
                        $data["EXAMNO2"]              = $tmp[$cnt++];
                    }
 
                    //エラーチェック
                    if ($this->errCheck($db, $data, $checkdata, $line)) {
                        $temp_arr[$i] = $data;
                        $i++;
                    }
                    $line++;
                }
            }

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
        $hissuErrFlg        = false;    //必須エラーフラグ
        $existenceErrFlg    = false;    //存在エラーフラグ
        $numericErrFlg      = false;    //数字エラーフラグ
        $lengthErrFlg       = false;    //レングスエラーフラグ
        $otherErrFlg        = false;    //その他エラーフラグ

        $hissuErrMsg        = "";       //必須エラーメッセージ
        $existenceErrMsg    = "";       //存在エラーメッセージ
        $numericErrMsg      = "";       //数字エラーメッセージ
        $lengthErrMsg       = "";       //レングスエラーメッセージ
        $otherErrMsg        = "";       //その他エラーメッセージ

        $hissuSep           = "";
        $existenceSep       = "";
        $existenceSep       = "";
        $numericSep         = "";
        $lengthSep          = "";
        $otherSep           = "";

        foreach ($checkdata as $key => $val) {
            // 更新
            if ($this->field["SHORI_MEI"] == "1") {
                //必須項目チェック
                if ($val[1] == "HISSU" && !strlen(trim($data[$key]))) {
                    $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[3]);
                }
                //数値チェック
                if (($val[2] == "INT" || $val[2] == "INT2") && $data[$key] != "" && !is_numeric($data[$key])) {
                    $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, $val[3]);
                }
                //チェック用にゼロ埋め
                if ($val[2] == "INT2" && $data[$key] != "" && is_numeric($data[$key])) {
                    $data[$key] = sprintf("%0{$val[0]}d", $data[$key]);
                }
                //存在チェック
                $array = array("ENTEXAMYEAR");
                if (in_array($key, $array)) {
                    if ($key == "ENTEXAMYEAR" && $this->ObjYear != $data[$key]) {
                        $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[3]);
                    }
                }
                //文字数チェック
                if (strlen($data[$key]) > $val[0]) {
                    $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[3]);
                }
                
                //日付チェック
                if ($val[2] == "DATE" && $data[$key] != "" && $this->date_rep($data[$key]) == "") {
                    $returnFlg = $this->errSet($otherErrFlg, $otherErrMsg, "※その他:", $otherSep, $val[3]);
                } elseif ($val[2] == "DATE2" && $data[$key] != "" && $this->date_rep2($data[$key]) == "") {
                    $returnFlg = $this->errSet($otherErrFlg, $otherErrMsg, "※その他:", $otherSep, $val[3]);
                }
                // 削除
            } else {
                //必須項目チェック
                $array = array("ENTEXAMYEAR", "LOGIN_USER_SEQ", "NAME_SEI", "NAME_MEI");
                if (in_array($key, $array) && !strlen(trim($data[$key]))) {
                    $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[3]);
                }
                //存在チェック
                $array = array("ENTEXAMYEAR");
                if (in_array($key, $array)) {
                    if ($key == "ENTEXAMYEAR" && $this->ObjYear != $data[$key]) {
                        $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[3]);
                    }
                }
            }
        }

        if (!$returnFlg) {
            $check_error = $hissuErrMsg.$numericErrMsg.$lengthErrMsg.$existenceErrMsg.$otherErrMsg;
            if (strlen($check_error) > 120) {
                $check_error = mb_strcut($check_error, 0, 115);
                $check_error .= "..他";
            }
            knjl714hQuery::insertQueryErr($db, $line, $check_error);
        }
        return $returnFlg;
    }

    //生年月日変換 項目チェック後に行う。
    public function date_rep($data)
    {
        if ($data != "") {
            $data = str_replace("/", "", $data);
            $data = substr($data, 0, 4)."-".substr($data, 4, 2)."-".substr($data, 6, 2);
            $data = common::DateConv1(str_replace("-", "/", $data), 4);
            if ($data) {
                $data = str_replace("/", "-", $data);
            }
        }

        return $data;
    }

    //テスト日付変換 項目チェック後に行う。
    public function date_rep2($data)
    {
        if ($data != "") {
            $data = str_replace("/", "-", $data);
            list($year, $month, $day) = explode("-", $data);
            $data = sprintf("%04d-%02d-%02d", $year, $month, $day);
            $data = common::DateConv1(str_replace("-", "/", $data), 4);
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

        //エラー項目
        $header_err = array("MSGROW"        => "行番号",
                            "MSGREMARK"     => "メッセージ");

        /**********************/
        /*  ヘッダー部分出力  */
        /**********************/

        $header = array();
        $header["ENTEXAMYEAR"]              = "※入試年度";
        $header["APPLICANTDIV"]             = "※学校種別";
        $header["APPNO"]                    = "出願申込番号";
        $header["LOGIN_USER_SEQ"]           = "※ログインユーザSEQ";
        $header["LOGIN_MAIL"]               = "ログインメールアドレス";
        $header["APP_MAIL"]                 = "申込時メールアドレス";
        $header["SHIGANSYA_SEQ"]            = "志願者SEQ";
        $header["NAME_SEI"]                 = "※志願者氏名（漢字）（姓）";
        $header["NAME_MEI"]                 = "※志願者氏名（漢字）（名）";
        $header["NAME_KANA_SEI"]            = "志願者氏名（カナ）（姓）";
        $header["NAME_KANA_MEI"]            = "志願者氏名（カナ）（名）";
        $header["SEX"]                      = "※性別";
        $header["BIRTHDAY"]                 = "※生年月日";
        $header["ZIPCD"]                    = "郵便番号";
        $header["PREF_NAME"]                = "都道府県";
        $header["CITY_NAME"]                = "市区町村";
        $header["BANCHI_NAME"]              = "町名・番地";
        $header["ADDRESS2"]                 = "建物名・部屋番号";
        $header["TELNO"]                    = "電話番号";
        $header["MIRAI_FS_CD"]              = "出身校コード";
        $header["MIRAI_FS_NAME"]            = "出身校名称";
        $header["MIRAI_FS_NAME_OTHER"]      = "出身校名称その他";
        if ($this->field["APPLICANTDIV"] == "2" && $this->field["TESTDIV1"] == 1) {
        } else {
            $header["MIRAI_SH_CD1"]             = "併願校1コード";
            $header["MIRAI_SH_NAME1"]           = "併願校1名称";
            $header["MIRAI_SH_NAME_OTHER1"]     = "併願校1名称その他";
            $header["MIRAI_SH_CD2"]             = "併願校2コード";
            $header["MIRAI_SH_NAME2"]           = "併願校2名称";
            $header["MIRAI_SH_NAME_OTHER2"]     = "併願校2名称その他";
            $header["MIRAI_SH_CD3"]             = "併願校3コード";
            $header["MIRAI_SH_NAME3"]           = "併願校3名称";
            $header["MIRAI_SH_NAME_OTHER3"]     = "併願校3名称その他";
        }
        $header["MIRAI_PS_CD"]              = "塾コード";
        $header["MIRAI_PS_NAME"]            = "塾名称";
        $header["MIRAI_PS_NAME_OTHER"]      = "塾名称その他";
        if ($this->field["APPLICANTDIV"] == "2" && $this->field["TESTDIV1"] == 1) {
            $header["QUALIFIED_ENG_NAME"]        = "【資格検定】英語検定※準２級以上保有者は入力";
            $header["QUALIFIED_KANJI_NAME"]      = "【資格検定】漢字検定※準２級以上保有者は入力";
            $header["QUALIFIED_MATH_NAME"]       = "【資格検定】実用数学技能検定※準２級以上保有者は入力";
            $header["QUALIFIED_READING_NAME"]    = "【資格検定】語彙・読解力検定※準２級以上保有者は入力";
            $header["ATHLETIC_PRIZE_NAME"]       = "【運動関係】賞状・プログラム等で選手登録及び大会成績の証明ができる者は選択";
            $header["CULTURE_PRIZE_NAME"]        = "【文化関係】賞状・プログラム等で出場・出店登録及び大会成績の証明ができる者は選択";
            $header["SCH_COUNCIL_PRIZE_NAME"]    = "【生徒会活動関係】全会員の投票により選出された者は選択";
            $header["NOTE"]                 = "空欄(注意書き)";
        } else {
            $header["EXTRA_ACTIVITYS"]      = "学級活動・クラブ活動・特技・受賞";
        }
        $header["GNAME_SEI"]                = "保護者氏名(漢字)（姓）";
        $header["GNAME_MEI"]                = "保護者氏名(漢字)（名）";
        $header["GKANA_SEI"]                = "保護者氏名(カナ)（姓）";
        $header["GKANA_MEI"]                = "保護者氏名(カナ)（名）";
        $header["GZIPCD"]                   = "※郵便番号";
        $header["GPREF_NAME"]               = "都道府県";
        $header["GCITY_NAME"]               = "市区町村";
        $header["GBANCHI_NAME"]             = "町名・番地";
        $header["GADDRESS2"]                = "建物名・部屋番号";
        $header["GTELNO"]                   = "緊急連絡先(電話番号)";
        $header["EXPLAIN"]                  = "緊急連絡先（説明）";
        $header["RELATIONSHIP"]             = "志願者との関係";
        $header["TONAI_CONTACT_ZIPCD"]      = "都内連絡先郵便番号※自宅より投稿受験の場合は不要";
        $header["TONAI_CONTACT_ADDRESS"]    = "都内連絡先住所※自宅より投稿受験の場合は不要";
        $header["TONAI_CONTACT_NAME"]       = "都内連絡先氏名※自宅より投稿受験の場合は不要";
        $header["TONAI_CONTACT_TELNO"]      = "都内連絡先電話番号※自宅より投稿受験の場合は不要";
        $header["APP_INFO_DIV"]             = "出願情報状態区分";
        $header["SRV_RCPT_DIV"]             = "調査書受領区分";
        $header["SRV_RCPT_MAIL_DATE"]       = "調査書受領メール送信日";
        $header["AGREE_FLG"]                = "同意フラグ";
        $header["COUNTER_RCPTNO"]           = "窓口受付番号";
        $header["REMARKS"]                  = "備考欄";
        $header["SETTLE_MONEY"]             = "決済金額";
        $header["EXAM_MONEY"]               = "受験料";
        $header["FEES"]                     = "事務手数料";
        $header["PAY_TYPE_CD"]              = "支払種別コード";
        $header["PAY_DUE_DATE"]             = "支払期限日";
        $header["PAY_DAY"]                  = "入金日";
        $header["PAY_DAY_MANUAL"]           = "入金日_手動";
        $header["PAY_INFO_DIV"]             = "入金情報区分";
        $header["APP_COMP_DAY"]             = "申込完了日";
        $header["CANCEL_FLG"]               = "キャンセルフラグ";
        $header["CANCEL_DATE"]              = "キャンセル日";
        $header["TEST_DATE1"]               = "試験日";
        $header["EXAMNO1"]                  = "受験番号";
        if ($this->field["APPLICANTDIV"] == "1") {
            $header["TEST_DATE2"]           = "試験日";
            $header["EXAMNO2"]              = "受験番号";
        }

        $contents  = "";        //クリア
        $header_data = false;   //出力フラグ

        if ($this->field["OUTPUT"] == "3") {
            //ヘッダ
            $header_data = true;
            $contents .= implode($header, ",")."\n";
        } elseif ($this->field["OUTPUT"] == "2") {
            //エラー
            $contents .= implode($header_err, ",")."\n";
        }


        /********************/
        /*  データ部分出力  */
        /********************/
        $db = Query::dbCheckOut();

        //ヘッダ以外
        if ($this->field["OUTPUT"] != "3") {
            //エラー
            $query = knjl714hQuery::selectCsvErrQuery();

            //SQL文発行
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                //エラー
                $setArray = $header_err;

                //ヘッダ順に値をセット
                $sep = "";
                foreach ($setArray as $field => $label) {
                    $contents .= $sep.$row[$field];
                    $sep = ",";
                }
                $contents .= "\n";

                $header_data = true;
            }
            $result->free();
        }

        Query::dbCheckIn($db);

        //ＣＳＶ出力
        if ($header_data) {
            if ($this->field["OUTPUT"] == "2") {      //エラー
                $couse = "エラー出力";
            } else {                                //エラー以外
                $couse = "志願者データ";
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
