<?php

require_once('for_php7.php');
class knjl310fModel extends Model
{
    public $field  = array();
    public $data   = array();
    public $cmd;
    public $file;
    public $ObjYear;
    public $lastColumn;//CSVの最後の列の値

    public function init()
    {
        $this->ObjYear = CTRL_YEAR+1;

        $this->field = array("OUTPUT"           =>  VARS::post("OUTPUT"),           //出力取込種別
                             "APPLICANTDIV"     =>  VARS::post("APPLICANTDIV"),     //入試制度
                             "TEST_L_DIV"       =>  VARS::post("TEST_L_DIV"),       //入試種類
                             "DATADIV"          =>  VARS::post("DATADIV"),          //データ種類
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
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
        $this->lastColumn = 'DUMMY';
        if ($this->Properties["csv_LastColumn"] != '') {
            $this->lastColumn = $this->Properties["csv_LastColumn"];
        }
    }

    /********************/
    /*  ＣＳＶ取込処理  */
    /********************/
    public function &getExecModel()
    {
        //ファイルの文字列の取得
        $this->data_arr = array();
        $this->data_arr = $this->getFileString();

        //データの整合性チェック
        if ($this->isDataMatched($this->data_arr)) {
            //中学・願書
            if ($this->field["APPLICANTDIV"] == "1" && $this->field["DATADIV"] == "1") {
                $cnt = knjl310fQuery::updateQueryCsv($this, $this->data_arr);

            //高校・願書
            } elseif ($this->field["APPLICANTDIV"] == "2" && $this->field["DATADIV"] == "1") {
                $cnt = knjl310fQuery::updateQueryCsv($this, $this->data_arr);

            //高校・調査書
            } elseif ($this->field["APPLICANTDIV"] == "2" && $this->field["DATADIV"] == "2") {
                $cnt = knjl310fQuery::updateQueryCsv2($this, $this->data_arr);
            }

            $db = Query::dbCheckOut();
            $errcnt = $db->getOne("SELECT COUNT(*) FROM W_CSVMSG_PRG_DAT WHERE PROGRAMID = '".PROGRAMID."' ");
            Query::dbCheckIn($db);

            $this->setMessage("MSG202", "処理済件数　：".$cnt."件\\nエラー件数    ：".$errcnt."件");
        }
    }

    /*************************/
    /*  CSVファイルを配列へ  */
    /*************************/
    public function getFileString()
    {
        $temp_arr = array();
        if (!is_uploaded_file($this->field["FILE"]["tmp_name"])) { //HTTP POSTによりアップロードされたファイルかどうかを調べる
            $this->warning = "ファイル名が不正です。";
        } else {
            $checkdata = array();
            $testdiv_array = $class_array = array();

            $db = Query::dbCheckOut();

            //中学・願書
            if ($this->field["APPLICANTDIV"] == "1" && $this->field["DATADIV"] == "1") {
                //入試区分
                $result = $db->query(knjl310fQuery::getNameMstAll($this->ObjYear, "L024"));
                while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    if ($this->field["TEST_L_DIV"] == "1" && $row["ABBV2"] == "1") {
                        $testdiv_array[$row["NAMECD2"]] = $row["NAME1"];
                    } elseif ($this->field["TEST_L_DIV"] == "2" && $row["ABBV2"] != "1") {
                        $testdiv_array[$row["NAMECD2"]] = $row["NAME1"];
                    }
                }

                $checkdata["ENTEXAMYEAR"]           = array(4,    "HISSU",  "INT",   "入試年度");
                $checkdata["APPLICANTDIV"]          = array(1,    "HISSU",  "INT",   "入試制度");
                $checkdata["TEST_L_DIV"]            = array(1,    "HISSU",  "INT",   "入試種類");
                $checkdata["DATADIV"]               = array(1,    "HISSU",  "INT",   "データ種類");
                $checkdata["RECEPTDATE"]            = array(10,   "HISSU",  "DATE",  "受付日付");
                $checkdata["GANSHO_YUUSOU"]         = array(1,    "",       "INT",   "願書郵送");
                $checkdata["EXAMNO"]                = array(5,    "HISSU",  "INT2",  "管理番号");
                $checkdata["NAME"]                  = array(60,   "HISSU",  "",      "氏名");
                $checkdata["NAME_KANA"]             = array(120,  "HISSU",  "",      "ふりがな");
                $checkdata["COURSECD"]              = array(1,    "HISSU",  "INT",   "課程");
                $checkdata["MAJORCD"]               = array(3,    "HISSU",  "INT2",  "学科");
                $checkdata["EXAMCOURSECD"]          = array(4,    "HISSU",  "INT2",  "志望コース");
                $checkdata["EXAM_PAY_DIV"]          = array(1,    "",       "INT",   "受験料入金方法");
                $checkdata["EXAM_PAY_DATE"]         = array(10,   "",       "DATE",  "受験料入金日");
                $checkdata["EXAM_PAY_CHAK_DATE"]    = array(10,   "",       "DATE",  "受験料着金日");
                $checkdata["RECRUIT_NO"]            = array(8,    "",       "INT2",  "事前番号");
                foreach ($testdiv_array as $key => $label) {
                    //帰国生
                    if ($this->field["TEST_L_DIV"] == "1") {
                        $checkdata["TESTDIV".$key."_FLG"]               = array(1,    "",      "INT",   $label);
                        $checkdata["TESTDIV".$key."_EXAM_TYPE"]         = array(1,    "",      "INT",   $label."受験科目");
                        $checkdata["TESTDIV".$key."_TESTSUBCLASSCD"]    = array(1,    "",      "INT",   $label."選択科目");
                        $checkdata["TESTDIV".$key."_RECEPTNO"]          = array(4,    "",      "INT2",  $label."受験番号");
                    //一般
                    } else {
                        $checkdata["TESTDIV".$key."_FLG"]               = array(1,    "",      "INT",   $label);
                        $checkdata["TESTDIV".$key."_EXAM_TYPE"]         = array(1,    "",      "INT",   $label."受験科目");
                        $checkdata["TESTDIV".$key."_RECEPTNO"]          = array(4,    "",      "INT2",  $label."受験番号");
                    }
                }
                $checkdata["BIRTHDAY"]              = array(10,   "",       "DATE",  "生年月日");
                $checkdata["ZIPCD"]                 = array(8,    "",       "",      "郵便番号");
                $checkdata["ADDRESS1"]              = array(90,   "",       "",      "現住所1");
                $checkdata["ADDRESS2"]              = array(90,   "",       "",      "現住所2");
                $checkdata["TELNO"]                 = array(14,   "",       "",      "電話番号");
                $checkdata["FS_CD"]                 = array(7,    "",       "INT2",  "出身学校コード");
                $checkdata["FS_DAY"]                = array(10,   "",       "DATE",  "卒業年月日");
                $checkdata["FS_GRDDIV"]             = array(1,    "",       "INT",   "既/卒");
                $checkdata["GNAME"]                 = array(60,   "",       "",      "保護者氏名");
                $checkdata["GKANA"]                 = array(120,  "",       "",      "保護者ふりがな");
                $checkdata["GZIPCD"]                = array(8,    "",       "",      "保護者郵便番号");
                $checkdata["GADDRESS1"]             = array(90,   "",       "",      "保護者現住所1");
                $checkdata["GADDRESS2"]             = array(90,   "",       "",      "保護者現住所2");
                $checkdata["GTELNO"]                = array(14,   "",       "",      "保護者電話番号");
                $checkdata["RELATIONSHIP"]          = array(2,    "",       "INT2",  "保護者続柄");
                $checkdata["SIS_DIV"]               = array(1,    "",       "INT",   "本校に姉妹が在学中");
                $checkdata["SIS_NAME"]              = array(60,   "",       "",      "姉妹氏名");
                $checkdata["SIS_JH"]                = array(1,    "",       "",      "姉妹中高区分");
                $checkdata["SIS_HR_CLASS"]          = array(6,    "",       "",      "姉妹クラス");
                $checkdata["MOM_NAME"]              = array(60,   "",       "",      "お母様の卒業時氏名(旧姓)");
                $checkdata["MOM_GRDYEAR"]           = array(4,    "",       "INT",   "お母様の卒業年度");
                $checkdata["MOM_HR_CLASS"]          = array(30,   "",       "",      "お母様のクラス");
                $checkdata["MOM_BIRTHDAY"]          = array(10,   "",       "DATE",  "お母様の生年月日");

            //高校・願書
            } elseif ($this->field["APPLICANTDIV"] == "2" && $this->field["DATADIV"] == "1") {
                $checkdata["ENTEXAMYEAR"]           = array(4,    "HISSU",  "INT",   "入試年度");
                $checkdata["APPLICANTDIV"]          = array(1,    "HISSU",  "INT",   "入試制度");
                $checkdata["DATADIV"]               = array(1,    "HISSU",  "INT",   "データ種類");
                $checkdata["RECEPTDATE"]            = array(10,   "HISSU",  "DATE",  "受付日付");
                $checkdata["GANSHO_YUUSOU"]         = array(1,    "",       "INT",   "願書郵送");
                $checkdata["EXAMNO"]                = array(4,    "HISSU",  "INT2",  "受験番号");
                $checkdata["NAME"]                  = array(60,   "HISSU",  "",      "氏名");
                $checkdata["NAME_KANA"]             = array(120,  "HISSU",  "",      "ふりがな");
                $checkdata["COURSECD"]              = array(1,    "HISSU",  "INT",   "課程");
                $checkdata["MAJORCD"]               = array(3,    "HISSU",  "INT2",  "学科");
                $checkdata["EXAMCOURSECD"]          = array(4,    "HISSU",  "INT2",  "志望コース");
                $checkdata["TESTDIV"]               = array(4,    "HISSU",  "INT",   "入試区分");
                $checkdata["TESTDIV0"]              = array(1,    "HISSU",  "INT",   "入試回数");
                $checkdata["EXAM_PAY_DIV"]          = array(1,    "",       "INT",   "受験料入金方法");
                $checkdata["EXAM_PAY_DATE"]         = array(10,   "",       "DATE",  "受験料入金日");
                $checkdata["EXAM_PAY_CHAK_DATE"]    = array(10,   "",       "DATE",  "受験料着金日");
                $checkdata["RECRUIT_NO"]            = array(8,    "",       "INT2",  "事前番号");
                $checkdata["SHDIV"]                 = array(1,    "HISSU",  "INT",   "本校が第1志望ですか");
                $checkdata["SHIFT_DESIRE_FLG"]      = array(1,    "",       "INT",   "入学手続の延期願");
                $checkdata["SH_SCHOOLNAME"]         = array(60,   "",       "",      "併願校名");
                $checkdata["SH_JUDGEMENT_DATE"]     = array(10,   "",       "DATE",  "合格発表日");
                $checkdata["SLIDE_FLG"]             = array(1,    "",       "INT",   "アドバンストへの変更");
                $checkdata["SELECT_SUBCLASS_DIV"]   = array(1,    "",       "INT",   "受験科目");
                $checkdata["BIRTHDAY"]              = array(10,   "",       "DATE",  "生年月日");
                $checkdata["ZIPCD"]                 = array(8,    "",       "",      "郵便番号");
                $checkdata["ADDRESS1"]              = array(90,   "",       "",      "現住所1");
                $checkdata["ADDRESS2"]              = array(90,   "",       "",      "現住所2");
                $checkdata["TELNO"]                 = array(14,   "",       "",      "電話番号");
                $checkdata["FS_CD"]                 = array(7,    "",       "INT2",  "出身学校コード");
                $checkdata["FS_DAY"]                = array(10,   "",       "DATE",  "卒業年月日");
                $checkdata["FS_GRDDIV"]             = array(1,    "",       "INT",   "既/卒");
                $checkdata["GNAME"]                 = array(60,   "",       "",      "保護者氏名");
                $checkdata["GKANA"]                 = array(120,  "",       "",      "保護者ふりがな");
                $checkdata["GZIPCD"]                = array(8,    "",       "",      "保護者郵便番号");
                $checkdata["GADDRESS1"]             = array(90,   "",       "",      "保護者現住所1");
                $checkdata["GADDRESS2"]             = array(90,   "",       "",      "保護者現住所2");
                $checkdata["GTELNO"]                = array(14,   "",       "",      "保護者電話番号");
                $checkdata["RELATIONSHIP"]          = array(2,    "",       "INT2",  "保護者続柄");
                $checkdata["SIS_DIV"]               = array(1,    "",       "INT",   "本校に姉妹が在学中");
                $checkdata["SIS_NAME"]              = array(60,   "",       "",      "姉妹氏名");
                $checkdata["SIS_JH"]                = array(1,    "",       "",      "姉妹中高区分");
                $checkdata["SIS_HR_CLASS"]          = array(6,    "",       "",      "姉妹クラス");
                $checkdata["MOM_NAME"]              = array(60,   "",       "",      "お母様の卒業時氏名(旧姓)");
                $checkdata["MOM_GRDYEAR"]           = array(4,    "",       "INT",   "お母様の卒業年度");
                $checkdata["MOM_HR_CLASS"]          = array(30,   "",       "",      "お母様のクラス");
                $checkdata["MOM_BIRTHDAY"]          = array(10,   "",       "DATE",  "お母様の生年月日");

            //高校・調査書
            } elseif ($this->field["APPLICANTDIV"] == "2" && $this->field["DATADIV"] == "2") {
                //教科
                $result = $db->query(knjl310fQuery::getNameMstAll($this->ObjYear, "L008"));
                while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    $class_array[$row["NAMECD2"]] = $row["ABBV1"];
                }

                $checkdata["ENTEXAMYEAR"]           = array(4,    "HISSU",  "INT",   "入試年度");
                $checkdata["APPLICANTDIV"]          = array(1,    "HISSU",  "INT",   "入試制度");
                $checkdata["DATADIV"]               = array(1,    "HISSU",  "INT",   "データ種類");
                $checkdata["EXAMNO"]                = array(4,    "HISSU",  "INT2",  "受験番号");
                foreach ($class_array as $classcd => $label) {
                    $checkdata["CLASSCD".$classcd]      = array(4,    "",       "INT",   $label);
                }
                $checkdata["TOTAL3"]                = array(4,    "",       "INT",   "3科計");
                $checkdata["TOTAL5"]                = array(4,    "",       "INT",   "5科計");
                $checkdata["TOTAL_ALL"]             = array(4,    "",       "INT",   "9科計");
                $checkdata["KASANTEN_ALL"]          = array(3,    "",       "INT",   "段階");
                $checkdata["REMARK1"]               = array(60,   "",       "",      "備考2");
            }

            $db->autoCommit(false);

            //エラーデータの削除
            $query = knjl310fQuery::deleteQueryErr();
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
                    $checkcount = get_count($tmp);

                    $data = array();

                    //中学・願書
                    if ($this->field["APPLICANTDIV"] == "1" && $this->field["DATADIV"] == "1") {
                        $data["ENTEXAMYEAR"]            = $tmp[0];
                        $data["APPLICANTDIV"]           = $tmp[1];
                        $data["TEST_L_DIV"]             = $tmp[2];
                        $data["DATADIV"]                = $tmp[3];
                        $data["RECEPTDATE"]             = $tmp[4];
                        $data["GANSHO_YUUSOU"]          = $tmp[5];
                        $data["EXAMNO"]                 = $tmp[6];
                        $data["NAME"]                   = $tmp[7];
                        $data["NAME_KANA"]              = $tmp[8];
                        $data["COURSECD"]               = $tmp[9];
                        $data["MAJORCD"]                = $tmp[10];
                        $data["EXAMCOURSECD"]           = $tmp[11];
                        $data["EXAM_PAY_DIV"]           = $tmp[12];
                        $data["EXAM_PAY_DATE"]          = $tmp[13];
                        $data["EXAM_PAY_CHAK_DATE"]     = $tmp[14];
                        $data["RECRUIT_NO"]             = $tmp[15];

                        $tmpCnt = 15;
                        foreach ($testdiv_array as $key => $label) {
                            //帰国生
                            if ($this->field["TEST_L_DIV"] == "1") {
                                $data["TESTDIV".$key."_FLG"]               = $tmp[++$tmpCnt];
                                $data["TESTDIV".$key."_EXAM_TYPE"]         = $tmp[++$tmpCnt];
                                $data["TESTDIV".$key."_TESTSUBCLASSCD"]    = $tmp[++$tmpCnt];
                                $data["TESTDIV".$key."_RECEPTNO"]          = $tmp[++$tmpCnt];
                            //一般
                            } else {
                                $data["TESTDIV".$key."_FLG"]               = $tmp[++$tmpCnt];
                                $data["TESTDIV".$key."_EXAM_TYPE"]         = $tmp[++$tmpCnt];
                                $data["TESTDIV".$key."_RECEPTNO"]          = $tmp[++$tmpCnt];
                            }
                        }
                        $data["BIRTHDAY"]               = $tmp[++$tmpCnt];
                        $data["ZIPCD"]                  = $tmp[++$tmpCnt];
                        $data["ADDRESS1"]               = $tmp[++$tmpCnt];
                        $data["ADDRESS2"]               = $tmp[++$tmpCnt];
                        $data["TELNO"]                  = $tmp[++$tmpCnt];
                        $data["FS_CD"]                  = $tmp[++$tmpCnt];
                        $data["FS_NAME"]                = $tmp[++$tmpCnt];
                        $data["FS_DAY"]                 = $tmp[++$tmpCnt];
                        $data["FS_GRDDIV"]              = $tmp[++$tmpCnt];
                        $data["GNAME"]                  = $tmp[++$tmpCnt];
                        $data["GKANA"]                  = $tmp[++$tmpCnt];
                        $data["GZIPCD"]                 = $tmp[++$tmpCnt];
                        $data["GADDRESS1"]              = $tmp[++$tmpCnt];
                        $data["GADDRESS2"]              = $tmp[++$tmpCnt];
                        $data["GTELNO"]                 = $tmp[++$tmpCnt];
                        $data["RELATIONSHIP"]           = $tmp[++$tmpCnt];
                        $data["SIS_DIV"]                = $tmp[++$tmpCnt];
                        $data["SIS_NAME"]               = $tmp[++$tmpCnt];
                        $data["SIS_JH"]                 = $tmp[++$tmpCnt];
                        $data["SIS_HR_CLASS"]           = $tmp[++$tmpCnt];
                        $data["MOM_NAME"]               = $tmp[++$tmpCnt];
                        $data["MOM_GRDYEAR"]            = $tmp[++$tmpCnt];
                        $data["MOM_HR_CLASS"]           = $tmp[++$tmpCnt];
                        $data["MOM_BIRTHDAY"]           = $tmp[++$tmpCnt];
                        $data[$this->lastColumn]        = $tmp[++$tmpCnt];

                    //高校・願書
                    } elseif ($this->field["APPLICANTDIV"] == "2" && $this->field["DATADIV"] == "1") {
                        $data["ENTEXAMYEAR"]            = $tmp[0];
                        $data["APPLICANTDIV"]           = $tmp[1];
                        $data["DATADIV"]                = $tmp[2];
                        $data["RECEPTDATE"]             = $tmp[3];
                        $data["GANSHO_YUUSOU"]          = $tmp[4];
                        $data["EXAMNO"]                 = $tmp[5];
                        $data["NAME"]                   = $tmp[6];
                        $data["NAME_KANA"]              = $tmp[7];
                        $data["COURSECD"]               = $tmp[8];
                        $data["MAJORCD"]                = $tmp[9];
                        $data["EXAMCOURSECD"]           = $tmp[10];
                        $data["TESTDIV"]                = $tmp[11];
                        $data["TESTDIV0"]               = $tmp[12];
                        $data["EXAM_PAY_DIV"]           = $tmp[13];
                        $data["EXAM_PAY_DATE"]          = $tmp[14];
                        $data["EXAM_PAY_CHAK_DATE"]     = $tmp[15];
                        $data["RECRUIT_NO"]             = $tmp[16];

                        $tmpCnt = 16;
                        $data["SHDIV"]                  = $tmp[++$tmpCnt];
                        $data["SHIFT_DESIRE_FLG"]       = $tmp[++$tmpCnt];
                        $data["SH_SCHOOLNAME"]          = $tmp[++$tmpCnt];
                        $data["SH_JUDGEMENT_DATE"]      = $tmp[++$tmpCnt];
                        $data["SLIDE_FLG"]              = $tmp[++$tmpCnt];
                        $data["SELECT_SUBCLASS_DIV"]    = $tmp[++$tmpCnt];
                        $data["BIRTHDAY"]               = $tmp[++$tmpCnt];
                        $data["ZIPCD"]                  = $tmp[++$tmpCnt];
                        $data["ADDRESS1"]               = $tmp[++$tmpCnt];
                        $data["ADDRESS2"]               = $tmp[++$tmpCnt];
                        $data["TELNO"]                  = $tmp[++$tmpCnt];
                        $data["FS_CD"]                  = $tmp[++$tmpCnt];
                        $data["FS_NAME"]                = $tmp[++$tmpCnt];
                        $data["FS_DAY"]                 = $tmp[++$tmpCnt];
                        $data["FS_GRDDIV"]              = $tmp[++$tmpCnt];
                        $data["GNAME"]                  = $tmp[++$tmpCnt];
                        $data["GKANA"]                  = $tmp[++$tmpCnt];
                        $data["GZIPCD"]                 = $tmp[++$tmpCnt];
                        $data["GADDRESS1"]              = $tmp[++$tmpCnt];
                        $data["GADDRESS2"]              = $tmp[++$tmpCnt];
                        $data["GTELNO"]                 = $tmp[++$tmpCnt];
                        $data["RELATIONSHIP"]           = $tmp[++$tmpCnt];
                        $data["SIS_DIV"]                = $tmp[++$tmpCnt];
                        $data["SIS_NAME"]               = $tmp[++$tmpCnt];
                        $data["SIS_JH"]                 = $tmp[++$tmpCnt];
                        $data["SIS_HR_CLASS"]           = $tmp[++$tmpCnt];
                        $data["MOM_NAME"]               = $tmp[++$tmpCnt];
                        $data["MOM_GRDYEAR"]            = $tmp[++$tmpCnt];
                        $data["MOM_HR_CLASS"]           = $tmp[++$tmpCnt];
                        $data["MOM_BIRTHDAY"]           = $tmp[++$tmpCnt];
                        $data[$this->lastColumn]        = $tmp[++$tmpCnt];

                    //高校・調査書
                    } elseif ($this->field["APPLICANTDIV"] == "2" && $this->field["DATADIV"] == "2") {
                        $data["ENTEXAMYEAR"]            = $tmp[0];
                        $data["APPLICANTDIV"]           = $tmp[1];
                        $data["DATADIV"]                = $tmp[2];
                        $data["EXAMNO"]                 = $tmp[3];

                        $tmpCnt = 3;
                        foreach ($class_array as $classcd => $label) {
                            $data["CLASSCD".$classcd]       = $tmp[++$tmpCnt];
                        }
                        $data["TOTAL3"]                 = $tmp[++$tmpCnt];
                        $data["TOTAL5"]                 = $tmp[++$tmpCnt];
                        $data["TOTAL_ALL"]              = $tmp[++$tmpCnt];
                        $data["KASANTEN_ALL"]           = $tmp[++$tmpCnt];
                        $data["REMARK1"]                = $tmp[++$tmpCnt];
                        $data[$this->lastColumn]        = $tmp[++$tmpCnt];
                    }

                    //項目数によるファイルチェック
                    if ($checkcount !=get_count($data)) {
                        $this->warning = "CSVファイルを確認して下さい。\\nフォーマットが一致していません。";
                        return;
                    }

                    //エラーチェック
                    if ($this->errCheck($db, $data, $checkdata, $line, $testdiv_array)) {
                        foreach ($checkdata as $key => $val) {
                            //日付を変換してセット
                            if ($val[2] == "DATE") {
                                $data[$key] = $this->dateRep($data[$key]);
                            }
                            //ゼロ埋めしてセット
                            if ($val[2] == "INT2" && $data[$key] != "" && is_numeric($data[$key])) {
                                $data[$key] = sprintf("%0{$val[0]}d", $data[$key]);
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
    public function errCheck($db, $data, $checkdata, $line, $testdiv_array)
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

        //受験番号範囲チェック用
        $checkNumber = array();
        if ($this->field["APPLICANTDIV"] == "1" && $this->field["DATADIV"] == "1") {
            $checkNumber = $this->checkNumberJ();
        } elseif ($this->field["APPLICANTDIV"] == "2" && $this->field["DATADIV"] == "1") {
            $checkNumber = $this->checkNumberH();
        }

        foreach ($checkdata as $key => $val) {
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
            $array = array("ENTEXAMYEAR","APPLICANTDIV","DATADIV");
            if ($this->field["APPLICANTDIV"] == "1") {
                array_push($array, "TEST_L_DIV");
            }
            if (in_array($key, $array)) {
                if (($key == "ENTEXAMYEAR" && $this->ObjYear != $data[$key]) || ($key != "ENTEXAMYEAR" && $this->field["$key"] != $data[$key])) {
                    $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[3]);
                }
            }
            if ($key == 'EXAMCOURSECD' && strlen($data[$key]) && 0 == $db->getOne(knjl310fQuery::examcoursecdCntSql($data["ENTEXAMYEAR"], $data["APPLICANTDIV"], $data["COURSECD"], $data["MAJORCD"], $data[$key]))) {
                $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[3]);
            }
            if ($key == "GANSHO_YUUSOU" && strlen($data[$key]) && $data[$key] != "1") {
                $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[3]);
            }
            if ($key == 'EXAM_PAY_DIV' && strlen($data[$key]) && !in_array($data[$key], array('1', '2'))) {
                $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[3]);
            }
            if ($key == 'SHDIV' && strlen($data[$key]) && 0 == $db->getOne(knjl310fQuery::nameCntSql("L006", $data[$key]))) {
                $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[3]);
            }
            if ($key == "SHIFT_DESIRE_FLG" && strlen($data[$key]) && $data[$key] != "1") {
                $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[3]);
            }
            if ($key == "SLIDE_FLG" && strlen($data[$key]) && $data[$key] != "1") {
                $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[3]);
            }
            $where = " AND NAMECD2 IN ('2','5') ";
            if ($key == 'SELECT_SUBCLASS_DIV' && strlen($data[$key]) && 0 == $db->getOne(knjl310fQuery::nameCntSql("L009", $data[$key], $where))) {
                $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[3]);
            }
            if ($key == 'FS_CD' && strlen($data[$key]) && 0 == $db->getOne(knjl310fQuery::fsCdCntSql($data[$key]))) {
                $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[3]);
            }
            if ($key == 'FS_GRDDIV' && strlen($data[$key]) && 0 == $db->getOne(knjl310fQuery::nameCntSql("L016", $data[$key]))) {
                $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[3]);
            }
            if ($key == 'RELATIONSHIP' && strlen($data[$key]) && 0 == $db->getOne(knjl310fQuery::nameCntSql("H201", $data[$key]))) {
                $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[3]);
            }
            if ($key == 'SIS_DIV' && strlen($data[$key]) && !in_array($data[$key], array('1', '2'))) {
                $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[3]);
            }
            if ($key == 'SIS_JH' && strlen($data[$key]) && 0 == $db->getOne(knjl310fQuery::sisJhCntSql($data[$key]))) {
                $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[3]);
            }
            if ($key == 'SIS_HR_CLASS' && strlen($data[$key]) && 0 == $db->getOne(knjl310fQuery::sisHrClassCntSql($data[$key], $data["SIS_JH"]))) {
                $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[3]);
            }

            if ($this->field["APPLICANTDIV"] == "1" && $this->field["DATADIV"] == "1") {
                foreach ($testdiv_array as $testdiv => $label) {
                    //入試区分
                    if ($key == "TESTDIV".$testdiv."_FLG" && strlen($data[$key]) && $data[$key] != "1") {
                        $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[3]);
                    }
                    if ($this->field["TEST_L_DIV"] == "1") {    //帰国生入試
                        //受験科目
                        if ($key == "TESTDIV".$testdiv."_EXAM_TYPE" && strlen($data[$key]) && !in_array($data[$key], array('4','5'))) {
                            $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[3]);
                        }
                        //選択科目
                        if ($data["TESTDIV".$testdiv."_EXAM_TYPE"] == "5" && $key == "TESTDIV".$testdiv."_TESTSUBCLASSCD" && strlen($data[$key]) && !in_array($data[$key], array('2','5'))) {
                            $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[3]);
                        }
                    } else {        //一般入試
                        if ($key == "TESTDIV".$testdiv."_EXAM_TYPE" && strlen($data[$key])) {
                            $err = false;
                            if (in_array($testdiv, array('1','2','3','4')) && !in_array($data[$key], array('1','2'))) {
                                $err = true;
                            } elseif ($testdiv == '5' && $data[$key] != '3') {
                                $err = true;
                            } elseif (in_array($testdiv, array('8','9')) && !in_array($data[$key], array('1','2','3'))) {
                                $err = true;
                            }
                            if ($err) {
                                $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[3]);
                            }
                        }
                    }
                }
            }

            //日付チェック
            if ($val[2] == "DATE" && $data[$key] != "" && $this->dateRep($data[$key]) == "") {
                $returnFlg = $this->errSet($otherErrFlg, $otherErrMsg, "※その他:", $otherSep, $val[3]);
            }

            //文字数チェック
            if (strlen($data[$key]) > $val[0]) {
                $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[3]);
            }
        }

        //使用不可チェック
        if (strlen($data["SLIDE_FLG"]) && !in_array($data["EXAMCOURSECD"], array('1002','2002'))) {
            $returnFlg = $this->errSet($otherErrFlg, $otherErrMsg, "※その他:", $otherSep, 'アドバンストへの変更不可');
        }
        if (strlen($data["SELECT_SUBCLASS_DIV"]) && !($data["EXAMCOURSECD"] == '3001' && in_array($data["TESTDIV"], array('1','4')))) {
            $returnFlg = $this->errSet($otherErrFlg, $otherErrMsg, "※その他:", $otherSep, '受験科目使用不可');
        }

        if ($this->field["APPLICANTDIV"] == "1" && $this->field["DATADIV"] == "1") {
            //重複チェック
            if ($this->field["TEST_L_DIV"] == "2" && $data["TESTDIV1_FLG"] && $data["TESTDIV2_FLG"]) {
                $returnFlg = $this->errSet($otherErrFlg, $otherErrMsg, "※その他:", $otherSep, '入試区分重複不可');
            }

            //入試区分チェック
            $testdiv_cnt = 0;
            foreach ($testdiv_array as $key => $label) {
                $testdiv_err = true;
                if ($data["TESTDIV".$key."_FLG"] && $data["TESTDIV".$key."_EXAM_TYPE"] && $data["TESTDIV".$key."_RECEPTNO"]) {
                    if ($this->field["TEST_L_DIV"] == "1" && $data["TESTDIV".$key."_EXAM_TYPE"] == "5") {
                        if ($data["TESTDIV".$key."_TESTSUBCLASSCD"]) {
                            $testdiv_cnt++;
                            $testdiv_err = false;
                        }
                    } else {
                        $testdiv_cnt++;
                        $testdiv_err = false;
                    }
                }

                if (!$data["TESTDIV".$key."_FLG"] && !$data["TESTDIV".$key."_EXAM_TYPE"] && !$data["TESTDIV".$key."_RECEPTNO"]) {
                    if ($this->field["TEST_L_DIV"] == "1") {
                        if (!$data["TESTDIV".$key."_TESTSUBCLASSCD"]) {
                            $testdiv_err = false;
                        }
                    } else {
                        $testdiv_err = false;
                    }
                }

                //入試区分の一部のみ入力
                if ($testdiv_err) {
                    $returnFlg = $this->errSet($otherErrFlg, $otherErrMsg, "※その他:", $otherSep, $label);
                }
            }
            //必須チェック
            if ($testdiv_cnt == 0) {
                $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, "入試区分未設定");
            }
        }

        //受験番号範囲チェック
        $checkNumber_err_cnt = 0;
        if ($this->field["APPLICANTDIV"] == "1" && $this->field["DATADIV"] == "1") {
            foreach ($testdiv_array as $key => $label) {
                if ($data["TESTDIV".$key."_FLG"] && $data["TESTDIV".$key."_EXAM_TYPE"] && $data["TESTDIV".$key."_RECEPTNO"]) {
                    foreach ($checkNumber as $key2 => $val2) {
                        if ($key == $val2["TESTDIV"] && $data["TESTDIV".$key."_EXAM_TYPE"] == $val2["EXAM_TYPE"]) {
                            if ($data["TESTDIV".$key."_RECEPTNO"] < $val2["MIN_NO"] || $data["TESTDIV".$key."_RECEPTNO"] > $val2["MAX_NO"]) {
                                $checkNumber_err_cnt++;
                            }
                        }
                    }
                }
            }
        } elseif ($this->field["APPLICANTDIV"] == "2" && $this->field["DATADIV"] == "1") {
            if ($data["TESTDIV"] && $data["TESTDIV0"] && $data["EXAMCOURSECD"] && $data["EXAMNO"]) {
                foreach ($checkNumber as $key2 => $val2) {
                    if ($data["TESTDIV"] == $val2["TESTDIV"] && $data["TESTDIV0"] == $val2["TESTDIV0"] && $data["EXAMCOURSECD"] == $val2["EXAMCOURSECD"]) {
                        if ($data["EXAMNO"] < $val2["MIN_NO"] || $data["EXAMNO"] > $val2["MAX_NO"]) {
                            $checkNumber_err_cnt++;
                        }
                    }
                }
            }
        }
        if ($checkNumber_err_cnt > 0) {
            $returnFlg = $this->errSet($otherErrFlg, $otherErrMsg, "※その他:", $otherSep, "受験番号範囲不可");
        }

        if ($this->field["APPLICANTDIV"] == "2" && $this->field["DATADIV"] == "2") {
            $base_cnt = $db->getOne(knjl310fQuery::cntEntexamApplicantbaseDat($this, $data));
            //存在チェック -- ENTEXAM_APPLICANTBASE_DAT
            if ($base_cnt == 0) {
                $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, "願書データ");
            }
        }

        //受験料の一部のみ入力
        $exam_pay_err = false;
        if (strlen($data["EXAM_PAY_DIV"]) && !strlen($data["EXAM_PAY_DATE"])) {
            $exam_pay_err = true;
        }
        if (strlen($data["EXAM_PAY_DATE"]) && !strlen($data["EXAM_PAY_DIV"])) {
            $exam_pay_err = true;
        }
        if ($exam_pay_err) {
            $returnFlg = $this->errSet($otherErrFlg, $otherErrMsg, "※その他:", $otherSep, "受験料");
        }

        if (!$returnFlg) {
            $check_error = $hissuErrMsg.$numericErrMsg.$lengthErrMsg.$existenceErrMsg.$otherErrMsg;
            if (strlen($check_error) > 120) {
                $check_error = mb_strcut($check_error, 0, 115);
                $check_error .= "..他";
            }
            knjl310fQuery::insertQueryErr($db, $line, $check_error);
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

    //受験番号範囲チェック
    public function checkNumberJ()
    {
        $check = array();
        $check[] = array("TESTDIV" => "1", "EXAM_TYPE" => "1", "MIN_NO" => "1001", "MAX_NO" => "1499");
        $check[] = array("TESTDIV" => "1", "EXAM_TYPE" => "2", "MIN_NO" => "1501", "MAX_NO" => "1999");
        $check[] = array("TESTDIV" => "2", "EXAM_TYPE" => "1", "MIN_NO" => "2001", "MAX_NO" => "2499");
        $check[] = array("TESTDIV" => "2", "EXAM_TYPE" => "2", "MIN_NO" => "2501", "MAX_NO" => "2999");
        $check[] = array("TESTDIV" => "3", "EXAM_TYPE" => "1", "MIN_NO" => "3001", "MAX_NO" => "3499");
        $check[] = array("TESTDIV" => "3", "EXAM_TYPE" => "2", "MIN_NO" => "3501", "MAX_NO" => "3999");
        $check[] = array("TESTDIV" => "4", "EXAM_TYPE" => "1", "MIN_NO" => "4001", "MAX_NO" => "4499");
        $check[] = array("TESTDIV" => "4", "EXAM_TYPE" => "2", "MIN_NO" => "4501", "MAX_NO" => "4999");
        $check[] = array("TESTDIV" => "5", "EXAM_TYPE" => "3", "MIN_NO" => "5001", "MAX_NO" => "5499");
        $check[] = array("TESTDIV" => "6", "EXAM_TYPE" => "4", "MIN_NO" => "7101", "MAX_NO" => "7199");
        $check[] = array("TESTDIV" => "6", "EXAM_TYPE" => "5", "MIN_NO" => "7101", "MAX_NO" => "7199");
        $check[] = array("TESTDIV" => "7", "EXAM_TYPE" => "4", "MIN_NO" => "7201", "MAX_NO" => "7299");
        $check[] = array("TESTDIV" => "7", "EXAM_TYPE" => "5", "MIN_NO" => "7201", "MAX_NO" => "7299");
        $check[] = array("TESTDIV" => "8", "EXAM_TYPE" => "1", "MIN_NO" => "6001", "MAX_NO" => "6499");
        $check[] = array("TESTDIV" => "8", "EXAM_TYPE" => "2", "MIN_NO" => "6501", "MAX_NO" => "6999");
        $check[] = array("TESTDIV" => "8", "EXAM_TYPE" => "3", "MIN_NO" => "6001", "MAX_NO" => "6499");
        $check[] = array("TESTDIV" => "9", "EXAM_TYPE" => "1", "MIN_NO" => "8001", "MAX_NO" => "8499");
        $check[] = array("TESTDIV" => "9", "EXAM_TYPE" => "2", "MIN_NO" => "8501", "MAX_NO" => "8999");
        $check[] = array("TESTDIV" => "9", "EXAM_TYPE" => "3", "MIN_NO" => "8001", "MAX_NO" => "8499");
        return $check;
    }
    //受験番号範囲チェック
    public function checkNumberH()
    {
        $check = array();
        //Ａ推薦
        $check[] = array("TESTDIV" => "1", "TESTDIV0" => "1", "EXAMCOURSECD" => "1001", "MIN_NO" => "1001", "MAX_NO" => "1099");
        $check[] = array("TESTDIV" => "1", "TESTDIV0" => "1", "EXAMCOURSECD" => "1002", "MIN_NO" => "1101", "MAX_NO" => "1199");
        $check[] = array("TESTDIV" => "1", "TESTDIV0" => "1", "EXAMCOURSECD" => "2001", "MIN_NO" => "1201", "MAX_NO" => "1299");
        $check[] = array("TESTDIV" => "1", "TESTDIV0" => "1", "EXAMCOURSECD" => "2002", "MIN_NO" => "1301", "MAX_NO" => "1399");
        $check[] = array("TESTDIV" => "1", "TESTDIV0" => "1", "EXAMCOURSECD" => "3001", "MIN_NO" => "1501", "MAX_NO" => "1599");
        //Ｂ推薦
        $check[] = array("TESTDIV" => "2", "TESTDIV0" => "1", "EXAMCOURSECD" => "1001", "MIN_NO" => "3001", "MAX_NO" => "3099");
        $check[] = array("TESTDIV" => "2", "TESTDIV0" => "1", "EXAMCOURSECD" => "1002", "MIN_NO" => "3101", "MAX_NO" => "3199");
        $check[] = array("TESTDIV" => "2", "TESTDIV0" => "1", "EXAMCOURSECD" => "2001", "MIN_NO" => "3201", "MAX_NO" => "3299");
        $check[] = array("TESTDIV" => "2", "TESTDIV0" => "1", "EXAMCOURSECD" => "2002", "MIN_NO" => "3301", "MAX_NO" => "3399");
        $check[] = array("TESTDIV" => "2", "TESTDIV0" => "1", "EXAMCOURSECD" => "3001", "MIN_NO" => "3501", "MAX_NO" => "3599");
        $check[] = array("TESTDIV" => "2", "TESTDIV0" => "2", "EXAMCOURSECD" => "1001", "MIN_NO" => "4001", "MAX_NO" => "4099");
        $check[] = array("TESTDIV" => "2", "TESTDIV0" => "2", "EXAMCOURSECD" => "1002", "MIN_NO" => "4101", "MAX_NO" => "4199");
        $check[] = array("TESTDIV" => "2", "TESTDIV0" => "2", "EXAMCOURSECD" => "2001", "MIN_NO" => "4201", "MAX_NO" => "4299");
        $check[] = array("TESTDIV" => "2", "TESTDIV0" => "2", "EXAMCOURSECD" => "2002", "MIN_NO" => "4301", "MAX_NO" => "4399");
        $check[] = array("TESTDIV" => "2", "TESTDIV0" => "2", "EXAMCOURSECD" => "3001", "MIN_NO" => "4501", "MAX_NO" => "4599");
        //一般（併願優遇含む）
        $check[] = array("TESTDIV" => "3", "TESTDIV0" => "1", "EXAMCOURSECD" => "1001", "MIN_NO" => "5001", "MAX_NO" => "5099");
        $check[] = array("TESTDIV" => "3", "TESTDIV0" => "1", "EXAMCOURSECD" => "1002", "MIN_NO" => "5101", "MAX_NO" => "5199");
        $check[] = array("TESTDIV" => "3", "TESTDIV0" => "1", "EXAMCOURSECD" => "2001", "MIN_NO" => "5201", "MAX_NO" => "5299");
        $check[] = array("TESTDIV" => "3", "TESTDIV0" => "1", "EXAMCOURSECD" => "2002", "MIN_NO" => "5301", "MAX_NO" => "5399");
        $check[] = array("TESTDIV" => "3", "TESTDIV0" => "1", "EXAMCOURSECD" => "3001", "MIN_NO" => "5501", "MAX_NO" => "5599");
        $check[] = array("TESTDIV" => "3", "TESTDIV0" => "2", "EXAMCOURSECD" => "1001", "MIN_NO" => "6001", "MAX_NO" => "6099");
        $check[] = array("TESTDIV" => "3", "TESTDIV0" => "2", "EXAMCOURSECD" => "1002", "MIN_NO" => "6101", "MAX_NO" => "6199");
        $check[] = array("TESTDIV" => "3", "TESTDIV0" => "2", "EXAMCOURSECD" => "2001", "MIN_NO" => "6201", "MAX_NO" => "6299");
        $check[] = array("TESTDIV" => "3", "TESTDIV0" => "2", "EXAMCOURSECD" => "2002", "MIN_NO" => "6301", "MAX_NO" => "6399");
        $check[] = array("TESTDIV" => "3", "TESTDIV0" => "2", "EXAMCOURSECD" => "3001", "MIN_NO" => "6501", "MAX_NO" => "6599");
        //帰国生Ａ方式
        $check[] = array("TESTDIV" => "4", "TESTDIV0" => "1", "EXAMCOURSECD" => "1001", "MIN_NO" => "7001", "MAX_NO" => "7099");
        $check[] = array("TESTDIV" => "4", "TESTDIV0" => "1", "EXAMCOURSECD" => "1002", "MIN_NO" => "7101", "MAX_NO" => "7199");
        $check[] = array("TESTDIV" => "4", "TESTDIV0" => "1", "EXAMCOURSECD" => "2001", "MIN_NO" => "7201", "MAX_NO" => "7299");
        $check[] = array("TESTDIV" => "4", "TESTDIV0" => "1", "EXAMCOURSECD" => "2002", "MIN_NO" => "7301", "MAX_NO" => "7399");
        $check[] = array("TESTDIV" => "4", "TESTDIV0" => "1", "EXAMCOURSECD" => "3001", "MIN_NO" => "7501", "MAX_NO" => "7599");
        $check[] = array("TESTDIV" => "4", "TESTDIV0" => "2", "EXAMCOURSECD" => "1001", "MIN_NO" => "7001", "MAX_NO" => "7099");
        $check[] = array("TESTDIV" => "4", "TESTDIV0" => "2", "EXAMCOURSECD" => "1002", "MIN_NO" => "7101", "MAX_NO" => "7199");
        $check[] = array("TESTDIV" => "4", "TESTDIV0" => "2", "EXAMCOURSECD" => "2001", "MIN_NO" => "7201", "MAX_NO" => "7299");
        $check[] = array("TESTDIV" => "4", "TESTDIV0" => "2", "EXAMCOURSECD" => "2002", "MIN_NO" => "7301", "MAX_NO" => "7399");
        $check[] = array("TESTDIV" => "4", "TESTDIV0" => "2", "EXAMCOURSECD" => "3001", "MIN_NO" => "7501", "MAX_NO" => "7599");
        //帰国生Ｂ方式
        $check[] = array("TESTDIV" => "5", "TESTDIV0" => "1", "EXAMCOURSECD" => "1001", "MIN_NO" => "7001", "MAX_NO" => "7099");
        $check[] = array("TESTDIV" => "5", "TESTDIV0" => "1", "EXAMCOURSECD" => "1002", "MIN_NO" => "7101", "MAX_NO" => "7199");
        $check[] = array("TESTDIV" => "5", "TESTDIV0" => "1", "EXAMCOURSECD" => "2001", "MIN_NO" => "7201", "MAX_NO" => "7299");
        $check[] = array("TESTDIV" => "5", "TESTDIV0" => "1", "EXAMCOURSECD" => "2002", "MIN_NO" => "7301", "MAX_NO" => "7399");
        $check[] = array("TESTDIV" => "5", "TESTDIV0" => "1", "EXAMCOURSECD" => "3001", "MIN_NO" => "7501", "MAX_NO" => "7599");
        $check[] = array("TESTDIV" => "5", "TESTDIV0" => "2", "EXAMCOURSECD" => "1001", "MIN_NO" => "7001", "MAX_NO" => "7099");
        $check[] = array("TESTDIV" => "5", "TESTDIV0" => "2", "EXAMCOURSECD" => "1002", "MIN_NO" => "7101", "MAX_NO" => "7199");
        $check[] = array("TESTDIV" => "5", "TESTDIV0" => "2", "EXAMCOURSECD" => "2001", "MIN_NO" => "7201", "MAX_NO" => "7299");
        $check[] = array("TESTDIV" => "5", "TESTDIV0" => "2", "EXAMCOURSECD" => "2002", "MIN_NO" => "7301", "MAX_NO" => "7399");
        $check[] = array("TESTDIV" => "5", "TESTDIV0" => "2", "EXAMCOURSECD" => "3001", "MIN_NO" => "7501", "MAX_NO" => "7599");
        //一貫生
        $check[] = array("TESTDIV" => "6", "TESTDIV0" => "1", "EXAMCOURSECD" => "1001", "MIN_NO" => "9101", "MAX_NO" => "9499");
        $check[] = array("TESTDIV" => "6", "TESTDIV0" => "1", "EXAMCOURSECD" => "1002", "MIN_NO" => "9101", "MAX_NO" => "9499");
        $check[] = array("TESTDIV" => "6", "TESTDIV0" => "1", "EXAMCOURSECD" => "2001", "MIN_NO" => "9101", "MAX_NO" => "9499");
        $check[] = array("TESTDIV" => "6", "TESTDIV0" => "1", "EXAMCOURSECD" => "2002", "MIN_NO" => "9101", "MAX_NO" => "9499");
        $check[] = array("TESTDIV" => "6", "TESTDIV0" => "1", "EXAMCOURSECD" => "3001", "MIN_NO" => "9101", "MAX_NO" => "9499");
        return $check;
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
    public function isDataMatched(&$data_arr)
    {
        //５千件を超えたら制限エラー
        if (get_count($data_arr) > 5000) {
            $this->setWarning("MSG915", "処理件数を５千件以下にして下さい 処理件数：".get_count($data_arr)."件");
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

        $header = $header_rei = array();
        $testdiv_array = $class_array = array();

        //エラー項目
        $header_err = array("MSGROW"        => "行番号",
                            "MSGREMARK"     => "メッセージ");

        /******************/
        /*  中学（願書）  */
        /******************/
        if ($this->field["APPLICANTDIV"] == "1" && $this->field["DATADIV"] == "1") {
            //入試区分
            $result = $db->query(knjl310fQuery::getNameMstAll($this->ObjYear, "L024"));
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                if ($this->field["TEST_L_DIV"] == "1" && $row["ABBV2"] == "1") {
                    $testdiv_array[$row["NAMECD2"]] = $row["NAME1"];
                } elseif ($this->field["TEST_L_DIV"] == "2" && $row["ABBV2"] != "1") {
                    $testdiv_array[$row["NAMECD2"]] = $row["NAME1"];
                }
            }

            /**************/
            /*  ヘッダー  */
            /**************/
            $header["ENTEXAMYEAR"]          = "※入試年度";
            $header["APPLICANTDIV"]         = "※入試制度";
            $header["TEST_L_DIV"]           = "※入試種類";
            $header["DATADIV"]              = "※データ種類";
            $header["RECEPTDATE"]           = "※受付日付";
            $header["GANSHO_YUUSOU"]        = "願書郵送";
            $header["EXAMNO"]               = "※管理番号";
            $header["NAME"]                 = "※氏名";
            $header["NAME_KANA"]            = "※ふりがな";
            $header["COURSECD"]             = "※課程";
            $header["MAJORCD"]              = "※学科";
            $header["EXAMCOURSECD"]         = "※志望コース";
            $header["EXAM_PAY_DIV"]         = "受験料入金方法";
            $header["EXAM_PAY_DATE"]        = "受験料入金日";
            $header["EXAM_PAY_CHAK_DATE"]   = "受験料着金日";
            $header["RECRUIT_NO"]           = "事前番号";
            foreach ($testdiv_array as $key => $label) {
                //帰国生
                if ($this->field["TEST_L_DIV"] == "1") {
                    $header["TESTDIV".$key."_FLG"]               = "※".$label;
                    $header["TESTDIV".$key."_EXAM_TYPE"]         = "※".$label."受験科目";
                    $header["TESTDIV".$key."_TESTSUBCLASSCD"]    = "※".$label."選択科目";
                    $header["TESTDIV".$key."_RECEPTNO"]          = "※".$label."受験番号";
                //一般
                } else {
                    $header["TESTDIV".$key."_FLG"]               = "※".$label;
                    $header["TESTDIV".$key."_EXAM_TYPE"]         = "※".$label."受験科目";
                    $header["TESTDIV".$key."_RECEPTNO"]          = "※".$label."受験番号";
                }
            }
            $header["BIRTHDAY"]             = "生年月日";
            $header["ZIPCD"]                = "郵便番号";
            $header["ADDRESS1"]             = "現住所1";
            $header["ADDRESS2"]             = "現住所2";
            $header["TELNO"]                = "電話番号";
            $header["FS_CD"]                = "出身学校コード";
            $header["FS_NAME"]              = "出身学校名";
            $header["FS_DAY"]               = "卒業年月日";
            $header["FS_GRDDIV"]            = "既/卒";
            $header["GNAME"]                = "保護者氏名";
            $header["GKANA"]                = "保護者ふりがな";
            $header["GZIPCD"]               = "保護者郵便番号";
            $header["GADDRESS1"]            = "保護者現住所1";
            $header["GADDRESS2"]            = "保護者現住所2";
            $header["GTELNO"]               = "保護者電話番号";
            $header["RELATIONSHIP"]         = "保護者続柄";
            $header["SIS_DIV"]              = "本校に姉妹が在学中";
            $header["SIS_NAME"]             = "姉妹氏名";
            $header["SIS_JH"]               = "姉妹中高区分";
            $header["SIS_HR_CLASS"]         = "姉妹クラス";
            $header["MOM_NAME"]             = "お母様の卒業時氏名(旧姓)";
            $header["MOM_GRDYEAR"]          = "お母様の卒業年度";
            $header["MOM_HR_CLASS"]         = "お母様のクラス";
            $header["MOM_BIRTHDAY"]         = "お母様の生年月日";
            $header["EIKEN_NAME"]           = "英検取得級";
            $header["JUDGE_KIND_NAME"]      = "特待生情報";
            $header[$this->lastColumn]      = $this->lastColumn;

            /****************/
            /*  ヘッダー例  */
            /****************/
            if ($this->field["TEST_L_DIV"] == "1") {
                //受験科目
                $where = " NAMECD2 IN ('4', '5') ";
                $exam_type = implode(' ', $db->getCol(knjl310fQuery::getHeaderReiNameMst($this->ObjYear, "L005", "NAME1", "", $where)));
                //選択科目
                $where = " NAMECD2 IN ('2', '5') ";
                $testsubclasscd = implode(' ', $db->getCol(knjl310fQuery::getHeaderReiNameMst($this->ObjYear, "L009", "NAME1", "", $where)));
            } elseif ($this->field["TEST_L_DIV"] == "2") {
                //受験科目（入試区分1～4）
                $where1 = " NAMECD2 IN ('1', '2') ";
                $exam_type1 = implode(' ', $db->getCol(knjl310fQuery::getHeaderReiNameMst($this->ObjYear, "L005", "NAME1", "", $where1)));
                //受験科目（入試区分5）
                $where2 = " NAMECD2 = '3' ";
                $exam_type2 = implode(' ', $db->getCol(knjl310fQuery::getHeaderReiNameMst($this->ObjYear, "L005", "NAME1", "", $where2)));
                //受験科目（入試区分8～9）
                $where3 = " NAMECD2 IN ('1','2','3') ";
                $exam_type3 = implode(' ', $db->getCol(knjl310fQuery::getHeaderReiNameMst($this->ObjYear, "L005", "NAME1", "", $where3)));
            }

            //既/卒
            $fs_grddiv = implode(' ', $db->getCol(knjl310fQuery::getHeaderReiNameMst($this->ObjYear, "L016", "NAME1")));
            //姉妹中高区分
            $sis_jh = implode(' ', $db->getCol(knjl310fQuery::getSisterJHName()));
            //英検取得級
            $eiken_name = implode(' ', $db->getCol(knjl310fQuery::getHeaderReiNameMst($this->ObjYear, "L055", "NAME1")));

            $yOldE = "12";  //入学年令
            $yOldG = "15";  //卒業年令
            $yOldM = "40";  //お母様年令

            $header_rei["ENTEXAMYEAR"]          = $this->ObjYear;
            $header_rei["APPLICANTDIV"]         = $this->field["APPLICANTDIV"];
            $header_rei["TEST_L_DIV"]           = $this->field["TEST_L_DIV"];
            $header_rei["DATADIV"]              = $this->field["DATADIV"];
            $header_rei["RECEPTDATE"]           = str_replace("-", "/", CTRL_DATE);
            $header_rei["GANSHO_YUUSOU"]        = "1:願書郵送";
            $header_rei["EXAMNO"]               = "12345";
            $header_rei["NAME"]                 = "アルプ太郎";
            $header_rei["NAME_KANA"]            = "あるぷたろう";
            $header_rei["COURSECD"]             = "1";
            $header_rei["MAJORCD"]              = "123";
            $header_rei["EXAMCOURSECD"]         = "1234";
            $header_rei["EXAM_PAY_DIV"]         = "1:振込 2:窓口";
            $header_rei["EXAM_PAY_DATE"]        = str_replace("-", "/", CTRL_DATE);
            $header_rei["EXAM_PAY_CHAK_DATE"]   = str_replace("-", "/", CTRL_DATE);
            $header_rei["RECRUIT_NO"]           = "12345678";
            foreach ($testdiv_array as $key => $label) {
                //帰国生
                if ($this->field["TEST_L_DIV"] == "1") {
                    $header_rei["TESTDIV".$key."_FLG"]              = "1:選択";
                    $header_rei["TESTDIV".$key."_EXAM_TYPE"]        = $exam_type;
                    $header_rei["TESTDIV".$key."_TESTSUBCLASSCD"]   = $testsubclasscd;
                    $header_rei["TESTDIV".$key."_RECEPTNO"]         = "1234";
                //一般
                } else {
                    $header_rei["TESTDIV".$key."_FLG"]              = "1:選択";
                    if (in_array($key, array('1','2','3','4'))) {
                        $header_rei["TESTDIV".$key."_EXAM_TYPE"]        = $exam_type1;
                    } elseif ($key == '5') {
                        $header_rei["TESTDIV".$key."_EXAM_TYPE"]        = $exam_type2;
                    } elseif (in_array($key, array('8','9'))) {
                        $header_rei["TESTDIV".$key."_EXAM_TYPE"]        = $exam_type3;
                    }
                    $header_rei["TESTDIV".$key."_RECEPTNO"]         = "1234";
                }
            }
            $header_rei["BIRTHDAY"]             = date('Y/m/d', strtotime(CTRL_DATE." -{$yOldE} years"));
            $header_rei["ZIPCD"]                = "123-4567";
            $header_rei["ADDRESS1"]             = "全角30文字";
            $header_rei["ADDRESS2"]             = "全角30文字";
            $header_rei["TELNO"]                = "12-3456-7890";
            $header_rei["FS_CD"]                = "1234567";
            $header_rei["FS_NAME"]              = "入力の必要なし";     //表示のみ
            $header_rei["FS_DAY"]               = CTRL_YEAR."/03/01";
            $header_rei["FS_GRDDIV"]            = $fs_grddiv;
            $header_rei["GNAME"]                = "アルプ父";
            $header_rei["GKANA"]                = "あるぷちち";
            $header_rei["GZIPCD"]               = "123-4567";
            $header_rei["GADDRESS1"]            = "全角30文字";
            $header_rei["GADDRESS2"]            = "全角30文字";
            $header_rei["GTELNO"]               = "123-456-7890";
            $header_rei["RELATIONSHIP"]         = "12";
            $header_rei["SIS_DIV"]              = "1:姉 2:妹";
            $header_rei["SIS_NAME"]             = "アルプ姉";
            $header_rei["SIS_JH"]               = $sis_jh;
            $header_rei["SIS_HR_CLASS"]         = "01-001";
            $header_rei["MOM_NAME"]             = "旧姓母";
            $header_rei["MOM_GRDYEAR"]          = CTRL_YEAR - ($yOldM - $yOldG);
            $header_rei["MOM_HR_CLASS"]         = "3年1組";
            $header_rei["MOM_BIRTHDAY"]         = date('Y/m/d', strtotime(CTRL_DATE." -{$yOldM} years"));
            $header_rei["EIKEN_NAME"]           = $eiken_name;
            $header_rei[$this->lastColumn]      = $this->lastColumn;
        }

        /******************/
        /*  高校（願書）  */
        /******************/
        if ($this->field["APPLICANTDIV"] == "2" && $this->field["DATADIV"] == "1") {
            /**************/
            /*  ヘッダー  */
            /**************/
            $header["ENTEXAMYEAR"]          = "※入試年度";
            $header["APPLICANTDIV"]         = "※入試制度";
            $header["DATADIV"]              = "※データ種類";
            $header["RECEPTDATE"]           = "※受付日付";
            $header["GANSHO_YUUSOU"]        = "願書郵送";
            $header["EXAMNO"]               = "※受験番号";
            $header["NAME"]                 = "※氏名";
            $header["NAME_KANA"]            = "※ふりがな";
            $header["COURSECD"]             = "※課程";
            $header["MAJORCD"]              = "※学科";
            $header["EXAMCOURSECD"]         = "※志望コース";
            $header["TESTDIV"]              = "※入試区分";
            $header["TESTDIV0"]             = "※入試回数";
            $header["EXAM_PAY_DIV"]         = "受験料入金方法";
            $header["EXAM_PAY_DATE"]        = "受験料入金日";
            $header["EXAM_PAY_CHAK_DATE"]   = "受験料着金日";
            $header["RECRUIT_NO"]           = "事前番号";
            $header["SHDIV"]                = "※本校が第1志望ですか";
            $header["SHIFT_DESIRE_FLG"]     = "入学手続の延期願";
            $header["SH_SCHOOLNAME"]        = "併願校名";
            $header["SH_JUDGEMENT_DATE"]    = "合格発表日";
            $header["SLIDE_FLG"]            = "Ｔクラスへの変更";
            $header["SELECT_SUBCLASS_DIV"]  = "受験科目";
            $header["BIRTHDAY"]             = "生年月日";
            $header["ZIPCD"]                = "郵便番号";
            $header["ADDRESS1"]             = "現住所1";
            $header["ADDRESS2"]             = "現住所2";
            $header["TELNO"]                = "電話番号";
            $header["FS_CD"]                = "出身学校コード";
            $header["FS_NAME"]              = "出身学校名";
            $header["FS_DAY"]               = "卒業年月日";
            $header["FS_GRDDIV"]            = "既/卒";
            $header["GNAME"]                = "保護者氏名";
            $header["GKANA"]                = "保護者ふりがな";
            $header["GZIPCD"]               = "保護者郵便番号";
            $header["GADDRESS1"]            = "保護者現住所1";
            $header["GADDRESS2"]            = "保護者現住所2";
            $header["GTELNO"]               = "保護者電話番号";
            $header["RELATIONSHIP"]         = "保護者続柄";
            $header["SIS_DIV"]              = "本校に姉妹が在学中";
            $header["SIS_NAME"]             = "姉妹氏名";
            $header["SIS_JH"]               = "姉妹中高区分";
            $header["SIS_HR_CLASS"]         = "姉妹クラス";
            $header["MOM_NAME"]             = "お母様の卒業時氏名(旧姓)";
            $header["MOM_GRDYEAR"]          = "お母様の卒業年度";
            $header["MOM_HR_CLASS"]         = "お母様のクラス";
            $header["MOM_BIRTHDAY"]         = "お母様の生年月日";
            $header["JUDGE_KIND_NAME"]      = "特待生情報";
            $header[$this->lastColumn]      = $this->lastColumn;

            /****************/
            /*  ヘッダー例  */
            /****************/
            //専併区分
            $shdiv = implode(' ', $db->getCol(knjl310fQuery::getHeaderReiNameMst($this->ObjYear, "L006", "NAME1")));
            //既/卒
            $fs_grddiv = implode(' ', $db->getCol(knjl310fQuery::getHeaderReiNameMst($this->ObjYear, "L016", "NAME1")));
            //受験科目
            $where = " NAMECD2 IN ('2','5') ";
            $select_subclass_div = implode(' ', $db->getCol(knjl310fQuery::getHeaderReiNameMst($this->ObjYear, "L009", "NAME2", "", $where)));
            //姉妹中高区分
            $sis_jh = implode(' ', $db->getCol(knjl310fQuery::getSisterJHName()));

            $yOldE = "15";  //入学年令
            $yOldG = "18";  //卒業年令
            $yOldM = "42";  //お母様年令

            $header_rei["ENTEXAMYEAR"]          = $this->ObjYear;
            $header_rei["APPLICANTDIV"]         = $this->field["APPLICANTDIV"];
            $header_rei["DATADIV"]              = $this->field["DATADIV"];
            $header_rei["RECEPTDATE"]           = str_replace("-", "/", CTRL_DATE);
            $header_rei["GANSHO_YUUSOU"]        = "1:願書郵送";
            $header_rei["EXAMNO"]               = "1234";
            $header_rei["NAME"]                 = "アルプ太郎";
            $header_rei["NAME_KANA"]            = "あるぷたろう";
            $header_rei["COURSECD"]             = "1";
            $header_rei["MAJORCD"]              = "123";
            $header_rei["EXAMCOURSECD"]         = "1234";
            $header_rei["TESTDIV"]              = "1";
            $header_rei["TESTDIV0"]             = "1";
            $header_rei["EXAM_PAY_DIV"]         = "1:振込 2:窓口";
            $header_rei["EXAM_PAY_DATE"]        = str_replace("-", "/", CTRL_DATE);
            $header_rei["EXAM_PAY_CHAK_DATE"]   = str_replace("-", "/", CTRL_DATE);
            $header_rei["RECRUIT_NO"]           = "12345678";
            $header_rei["SHDIV"]                = $shdiv;
            $header_rei["SHIFT_DESIRE_FLG"]     = "1:提出する";
            $header_rei["SH_SCHOOLNAME"]        = "全角20文字";
            $header_rei["SH_JUDGEMENT_DATE"]    = str_replace("-", "/", CTRL_DATE);
            $header_rei["SLIDE_FLG"]            = "1:希望する";
            $header_rei["SELECT_SUBCLASS_DIV"]  = $select_subclass_div;
            $header_rei["BIRTHDAY"]             = date('Y/m/d', strtotime(CTRL_DATE." -{$yOldE} years"));
            $header_rei["ZIPCD"]                = "123-4567";
            $header_rei["ADDRESS1"]             = "全角30文字";
            $header_rei["ADDRESS2"]             = "全角30文字";
            $header_rei["TELNO"]                = "12-3456-7890";
            $header_rei["FS_CD"]                = "1234567";
            $header_rei["FS_NAME"]              = "入力の必要なし";     //表示のみ
            $header_rei["FS_DAY"]               = CTRL_YEAR."/03/01";
            $header_rei["FS_GRDDIV"]            = $fs_grddiv;
            $header_rei["GNAME"]                = "アルプ父";
            $header_rei["GKANA"]                = "あるぷちち";
            $header_rei["GZIPCD"]               = "123-4567";
            $header_rei["GADDRESS1"]            = "全角30文字";
            $header_rei["GADDRESS2"]            = "全角30文字";
            $header_rei["GTELNO"]               = "123-456-7890";
            $header_rei["RELATIONSHIP"]         = "12";
            $header_rei["SIS_DIV"]              = "1:姉 2:妹";
            $header_rei["SIS_NAME"]             = "アルプ姉";
            $header_rei["SIS_JH"]               = $sis_jh;
            $header_rei["SIS_HR_CLASS"]         = "01-001";
            $header_rei["MOM_NAME"]             = "旧姓母";
            $header_rei["MOM_GRDYEAR"]          = CTRL_YEAR - ($yOldM - $yOldG);
            $header_rei["MOM_HR_CLASS"]         = "3年1組";
            $header_rei["MOM_BIRTHDAY"]         = date('Y/m/d', strtotime(CTRL_DATE." -{$yOldM} years"));
            $header_rei[$this->lastColumn]      = $this->lastColumn;
        }

        /********************/
        /*  高校（調査書）  */
        /********************/
        if ($this->field["APPLICANTDIV"] == "2" && $this->field["DATADIV"] == "2") {
            //教科
            $result = $db->query(knjl310fQuery::getNameMstAll($this->ObjYear, "L008"));
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $class_array[$row["NAMECD2"]] = $row["ABBV1"];
            }

            /**************/
            /*  ヘッダー  */
            /**************/
            $header["ENTEXAMYEAR"]          = "※入試年度";
            $header["APPLICANTDIV"]         = "※入試制度";
            $header["DATADIV"]              = "※データ種類";
            $header["EXAMNO"]               = "※受験番号";
            foreach ($class_array as $key => $label) {
                $header["CLASSCD".$key]     = $label;
            }
            $header["TOTAL3"]               = "3科計";
            $header["TOTAL5"]               = "5科計";
            $header["TOTAL_ALL"]            = "9科計";
            $header["KASANTEN_ALL"]         = "段階";
            $header["REMARK1"]              = "備考2";
            $header[$this->lastColumn]      = $this->lastColumn;

            /****************/
            /*  ヘッダー例  */
            /****************/
            $header_rei["ENTEXAMYEAR"]          = $this->ObjYear;
            $header_rei["APPLICANTDIV"]         = $this->field["APPLICANTDIV"];
            $header_rei["DATADIV"]              = $this->field["DATADIV"];
            $header_rei["EXAMNO"]               = "1234";
            foreach ($class_array as $key => $label) {
                $header_rei["CLASSCD".$key]     = "123";
            }
            $header_rei["TOTAL3"]               = "1234";
            $header_rei["TOTAL5"]               = "1234";
            $header_rei["TOTAL_ALL"]            = "1234";
            $header_rei["KASANTEN_ALL"]         = "123";
            $header_rei["REMARK1"]              = "全角20文字";
            $header_rei[$this->lastColumn]      = $this->lastColumn;
        }

        /**********************/
        /*  ヘッダー部分出力  */
        /**********************/
        $contents  = "";        //クリア
        $header_data = false;   //出力フラグ
        if ($this->field["OUTPUT"] == "1") {
            // ヘッダ出力
            $header_data = true;
            $contents .= implode($header, ",")."\n";
            $contents .= implode($header_rei, ",")."\n";
        } elseif ($this->field["OUTPUT"] == "3") {
            // エラー出力
            $contents .= implode($header_err, ",")."\n";
        } elseif ($this->field["OUTPUT"] == "4") {
            // データ出力
            if ($this->field["HEADER"] == "on") {
                $contents .= implode($header, ",")."\n";
            }
        }

        /********************/
        /*  データ部分出力  */
        /********************/
        if ($this->field["OUTPUT"] != "1") {
            $query = "";
            if ($this->field["OUTPUT"] == "3") {
                //エラー
                $query = knjl310fQuery::selectCsvErrQuery();
            } elseif ($this->field["OUTPUT"] == "4") {
                //データ
                if ($this->field["APPLICANTDIV"] == "1" && $this->field["DATADIV"] == "1") {
                    //中学・願書
                    $query = knjl310fQuery::selectMainQuery1($this, $testdiv_array);
                } elseif ($this->field["APPLICANTDIV"] == "2" && $this->field["DATADIV"] == "1") {
                    //高校・願書
                    $query = knjl310fQuery::selectMainQuery2_1($this);
                } elseif ($this->field["APPLICANTDIV"] == "2" && $this->field["DATADIV"] == "2") {
                    //高校・調査書
                    $query = knjl310fQuery::selectMainQuery2_2($this, $class_array);
                }
            }

            if ($this->field["DATADIV"] == "1") {
                //日付変換項目一覧
                $date_array = array("RECEPTDATE","EXAM_PAY_DATE","EXAM_PAY_CHAK_DATE","BIRTHDAY","FS_DAY","MOM_BIRTHDAY");
                if ($this->field["APPLICANTDIV"] == "2") {
                    array_push($date_array, "SH_JUDGEMENT_DATE");
                }
            }

            //SQL文発行
            if ($query) {
                $result = $db->query($query);
                while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    if ($this->field["OUTPUT"] == "4") {     //データ
                        if ($this->field["DATADIV"] == "1") { //願書
                            //日付変換
                            foreach ($date_array as $key) {
                                $row[$key] = str_replace("-", "/", $row[$key]);
                            }
                        }
                        $row = str_replace("\r\n", "", str_replace(",", ".", $row));   //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
                    }
                    $contents .= implode(",", $row) ."\n";
                    $header_data = true;
                }
                $result->free();
            }
        }

        /**************************/
        /*  ファイル名用名称取得  */
        /**************************/
        //入試制度名称
        $AppName = $db->getOne(knjl310fQuery::getNameMst($this->ObjYear, "L003", $this->field["APPLICANTDIV"]));
        //入試種類名称
        $TestLDivName = "";
        if ($this->field["APPLICANTDIV"] == "1") {
            $TestLDivName = $db->getOne(knjl310fQuery::getTestLDiv($this->ObjYear, "L024", $this->field["TEST_L_DIV"]));
        }
        //データ種類
        $datadiv = array("1" => "願書", "2" => "調査書");
        $DataDivName = $datadiv[$this->field["DATADIV"]];
        //ファイル名
        $title = $this->ObjYear.'年度 '.$AppName.' '.$TestLDivName.$DataDivName.'データ';

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
