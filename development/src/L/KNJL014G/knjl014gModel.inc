<?php

require_once('for_php7.php');
class knjl014gModel extends Model
{
    public $field  = array();
    public $data   = array();
    public $cmd;
    public $file;
    public $ObjYear;
    public $isKeiai;
    public $isKasiwara;
    public $lastColumn;//CSVの最後の列の値

    public function init()
    {
        $this->ObjYear = CTRL_YEAR+1;

        $this->field = array("OUTPUT"           =>  VARS::post("OUTPUT"),           //出力取込種別
                             "APPLICANTDIV"     =>  VARS::post("APPLICANTDIV"),     //入試制度
                             "DATADIV"          =>  VARS::post("DATADIV"),          //データ種類
                             "HEADER"           =>  VARS::post("HEADER"),           //ヘッダ有
                             "SHORI_MEI"        =>  VARS::post("SHORI_MEI")         //処理名
                             );

        $this->cmd = VARS::request("cmd");

        if (VARS::file("FILE")) {
            $this->field["FILE"] = VARS::file("FILE");      //ファイルのアップロード
            $this->field["FILE"]["name"] = mb_convert_encoding($this->field["FILE"]["name"], "SJIS-win", "UTF-8");
        }

        //学校コードを取得　敬愛：30270254001、柏原：30270247001
        $rtnRow = knjl014gQuery::getNameMstZ010();
        $this->isKeiai      = $rtnRow["NAME2"] == "30270254001" ? "1" : "";
        $this->isKasiwara   = $rtnRow["NAME2"] == "30270247001" ? "1" : "";

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
            //願書
            if ($this->field["DATADIV"] == "1") {
                $cnt = knjl014gQuery::updateQueryCsv($this, $this->data_arr);

            //調査書
            } elseif ($this->field["DATADIV"] == "2") {
                $cnt = knjl014gQuery::updateQueryCsv2($this, $this->data_arr);
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
            $class_array = array();

            $db = Query::dbCheckOut();

            //願書
            if ($this->field["DATADIV"] == "1") {
                $checkdata["ENTEXAMYEAR"]               = array(4,    "HISSU",  "INT",   "入試年度");
                $checkdata["APPLICANTDIV"]              = array(1,    "HISSU",  "INT",   "入試制度");
                $checkdata["DATADIV"]                   = array(1,    "HISSU",  "INT",   "データ種類");
                $checkdata["EXAMNO"]                    = array(4,    "HISSU",  "INT2",  "受験番号");
                $checkdata["RECEPTDATE"]                = array(10,   "HISSU",  "DATE",  "受付日付");
                $checkdata["TESTDIV"]                   = array(4,    "HISSU",  "INT",   "入試区分");
                $checkdata["SHDIV"]                     = array(1,    "HISSU",  "INT",   "専併区分");
                $checkdata["DAI1_MAJORCD"]              = array(3,    "HISSU",  "INT2",  "第１志望学科");
                $checkdata["DAI1_COURSECODE"]           = array(4,    "HISSU",  "INT2",  "第１志望受験コース");
                $checkdata["DAI2_MAJORCD"]              = array(3,    "",       "INT2",  "第２志望学科");
                $checkdata["DAI2_COURSECODE"]           = array(4,    "",       "INT2",  "第２志望受験コース");
                //柏原のみ
                if ($this->isKasiwara == "1") {
                    $checkdata["DAI2_SENPEIDIV"]            = array(1,    "",       "INT",   "第２志望専併区分");
                    $checkdata["HOPE_CLUB_CD"]              = array(2,    "",       "INT2",  "志望クラブコード");
                }
                $checkdata["NAME"]                      = array(60,   "HISSU",  "",      "氏名");
                $checkdata["NAME_KANA"]                 = array(120,  "HISSU",  "",      "ふりがな");
                $checkdata["SEX"]                       = array(1,    "HISSU",  "INT",   "性別");
                $checkdata["BIRTHDAY"]                  = array(10,   "",       "DATE",  "生年月日");
                $checkdata["ZIPCD"]                     = array(8,    "",       "",      "郵便番号");
                $checkdata["ADDRESS1"]                  = array(90,   "",       "",      "住所");
                $checkdata["ADDRESS2"]                  = array(90,   "",       "",      "方書");
                $checkdata["TELNO"]                     = array(14,   "",       "",      "電話番号");
                $checkdata["FS_CD"]                     = array(7,    "",       "INT2",  "出身学校コード");
                $checkdata["FS_DAY"]                    = array(7,   "",        "DATE",  "卒業年月");
                $checkdata["FS_GRDDIV"]                 = array(1,    "",       "INT",   "卒業区分");
                $checkdata["GNAME"]                     = array(60,   "",       "",      "保護者氏名");
                $checkdata["GKANA"]                     = array(120,  "",       "",      "保護者ふりがな");
                $checkdata["GZIPCD"]                    = array(8,    "",       "",      "保護者郵便番号");
                $checkdata["GADDRESS1"]                 = array(90,   "",       "",      "保護者住所");
                $checkdata["GADDRESS2"]                 = array(90,   "",       "",      "保護者方書");
                $checkdata["GTELNO"]                    = array(14,   "",       "",      "保護者電話番号");
                $checkdata["RELATIONSHIP"]              = array(2,    "",       "INT2",  "保護者続柄");
                $checkdata["JIZEN_SOUDAN_CD"]           = array(1,    "",       "INT",   "事前相談情報");
                $checkdata["JIZEN_SOUDAN_TEXT"]         = array(15,   "",       "",      "その他");
                $checkdata["CLUB_CD"]                   = array(2,    "",       "INT2",  "クラブコード");
                $checkdata["CLUB_RANK"]                 = array(1,    "",       "INT",   "クラブランク");
                $checkdata["SINZOKU_NAME"]              = array(60,   "",       "",      "親族氏名");
                $checkdata["SINZOKU_KYUSEI"]            = array(60,   "",       "",      "親族旧姓");
                $checkdata["SINZOKU_ZOKUGARA"]          = array(2,    "",       "INT2",  "親族続柄");
                $checkdata["SINZOKU_GAKKOUMEI"]         = array(60,   "",       "",      "親族学校名");
                $checkdata["SINZOKU_GAKKA"]             = array(60,   "",       "",      "親族学科・科");
                $checkdata["SINZOKU_ZAI_GRD_YEAR"]      = array(60,   "",       "",      "親族在学・卒業年");
                $checkdata["SINZOKU_ZAI_GRD_FLG"]       = array(1,    "",       "INT",   "親族在学・卒業区分");
                $checkdata["SINZOKU_FUTAGO_NAME"]       = array(60,   "",       "",      "双生児氏名");
                $checkdata["SINZOKU_FUTAGO_ZOKUGARA"]   = array(2,    "",       "INT2",  "双生児氏名");
                $checkdata["INTERVIEW_REMARK"]          = array(180,  "",       "",      "面接情報");
                $checkdata["INTERVIEW_VALUE"]           = array(1,    "",       "INT",   "面接評価");
                $checkdata["REMARK1"]                   = array(180,  "",       "",      "気づき情報");

            //調査書
            } elseif ($this->field["DATADIV"] == "2") {
                //教科
                $result = $db->query(knjl014gQuery::getNameMstAll($this->ObjYear, "L008"));
                while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    $class_array[$row["NAMECD2"]] = $row["ABBV1"];
                }

                $checkdata["ENTEXAMYEAR"]           = array(4,    "HISSU",  "INT",   "入試年度");
                $checkdata["APPLICANTDIV"]          = array(1,    "HISSU",  "INT",   "入試制度");
                $checkdata["DATADIV"]               = array(1,    "HISSU",  "INT",   "データ種類");
                $checkdata["EXAMNO"]                = array(4,    "HISSU",  "INT2",  "受験番号");
                foreach ($class_array as $classcd => $label) {
                    $checkdata["CLASSCD".$classcd]      = array(2,    "",       "INT",   "内申_".$label);
                }
                $checkdata["TOTAL_ALL"]             = array(3,    "",       "INT",   "内申_合計");
                $checkdata["AVERAGE_ALL"]           = array(4,    "",       "FLOAT", "内申_平均");
                $checkdata["ABSENCE_DAYS"]          = array(3,    "",       "INT",   "1年生欠席日数");
                $checkdata["ABSENCE_REMARK"]        = array(30,   "",       "",      "1年生欠席理由");
                $checkdata["ABSENCE_DAYS2"]         = array(3,    "",       "INT",   "2年生欠席日数");
                $checkdata["ABSENCE_REMARK2"]       = array(30,   "",       "",      "2年生欠席理由");
                $checkdata["ABSENCE_DAYS3"]         = array(3,    "",       "INT",   "3年生欠席日数");
                $checkdata["ABSENCE_REMARK3"]       = array(30,   "",       "",      "3年生欠席理由");
                $checkdata["JITURYOKU_KOKUGO"]      = array(3,    "",       "INT",   "実力テスト_国語");
                $checkdata["JITURYOKU_EIGO"]        = array(3,    "",       "INT",   "実力テスト_英語");
                $checkdata["JITURYOKU_SUUGAKU"]     = array(3,    "",       "INT",   "実力テスト_数学");
                $checkdata["JITURYOKU_TOTAL"]       = array(3,    "",       "INT",   "実力テスト_合計");
                $checkdata["MOGI_HENSATI"]          = array(4,    "",       "FLOAT", "模擬試験_偏差値");
            }

            $db->autoCommit(false);

            //エラーデータの削除
            $query = knjl014gQuery::deleteQueryErr();
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

                    //願書
                    if ($this->field["DATADIV"] == "1") {
                        $data["ENTEXAMYEAR"]                = $tmp[0];
                        $data["APPLICANTDIV"]               = $tmp[1];
                        $data["DATADIV"]                    = $tmp[2];
                        $data["EXAMNO"]                     = $tmp[3];

                        $tmpCnt = 3;
                        $data["RECEPTDATE"]                 = $tmp[++$tmpCnt];
                        $data["TESTDIV"]                    = $tmp[++$tmpCnt];
                        $data["SHDIV"]                      = $tmp[++$tmpCnt];
                        $data["DAI1_MAJORCD"]               = $tmp[++$tmpCnt];
                        $data["DAI1_COURSECODE"]            = $tmp[++$tmpCnt];
                        $data["DAI1_COURSE_NAME"]           = $tmp[++$tmpCnt];
                        $data["DAI2_MAJORCD"]               = $tmp[++$tmpCnt];
                        $data["DAI2_COURSECODE"]            = $tmp[++$tmpCnt];
                        $data["DAI2_COURSE_NAME"]           = $tmp[++$tmpCnt];
                        //柏原のみ
                        if ($this->isKasiwara == "1") {
                            $data["DAI2_SENPEIDIV"]             = $tmp[++$tmpCnt];
                            $data["HOPE_CLUB_CD"]               = $tmp[++$tmpCnt];
                            $data["HOPE_CLUB_NAME"]             = $tmp[++$tmpCnt];
                        }
                        $data["NAME"]                       = $tmp[++$tmpCnt];
                        $data["NAME_KANA"]                  = $tmp[++$tmpCnt];
                        $data["SEX"]                        = $tmp[++$tmpCnt];
                        $data["BIRTHDAY"]                   = $tmp[++$tmpCnt];
                        $data["ZIPCD"]                      = $tmp[++$tmpCnt];
                        $data["ADDRESS1"]                   = $tmp[++$tmpCnt];
                        $data["ADDRESS2"]                   = $tmp[++$tmpCnt];
                        $data["TELNO"]                      = $tmp[++$tmpCnt];
                        $data["FS_CD"]                      = $tmp[++$tmpCnt];
                        $data["FS_NAME"]                    = $tmp[++$tmpCnt];
                        $data["FS_DAY"]                     = $tmp[++$tmpCnt];
                        $data["FS_GRDDIV"]                  = $tmp[++$tmpCnt];
                        $data["GNAME"]                      = $tmp[++$tmpCnt];
                        $data["GKANA"]                      = $tmp[++$tmpCnt];
                        $data["GZIPCD"]                     = $tmp[++$tmpCnt];
                        $data["GADDRESS1"]                  = $tmp[++$tmpCnt];
                        $data["GADDRESS2"]                  = $tmp[++$tmpCnt];
                        $data["GTELNO"]                     = $tmp[++$tmpCnt];
                        $data["RELATIONSHIP"]               = $tmp[++$tmpCnt];
                        $data["JIZEN_SOUDAN_CD"]            = $tmp[++$tmpCnt];
                        $data["JIZEN_SOUDAN_TEXT"]          = $tmp[++$tmpCnt];
                        $data["CLUB_CD"]                    = $tmp[++$tmpCnt];
                        $data["CLUB_NAME"]                  = $tmp[++$tmpCnt];
                        $data["CLUB_RANK"]                  = $tmp[++$tmpCnt];
                        $data["SINZOKU_NAME"]               = $tmp[++$tmpCnt];
                        $data["SINZOKU_KYUSEI"]             = $tmp[++$tmpCnt];
                        $data["SINZOKU_ZOKUGARA"]           = $tmp[++$tmpCnt];
                        $data["SINZOKU_GAKKOUMEI"]          = $tmp[++$tmpCnt];
                        $data["SINZOKU_GAKKA"]              = $tmp[++$tmpCnt];
                        $data["SINZOKU_ZAI_GRD_YEAR"]       = $tmp[++$tmpCnt];
                        $data["SINZOKU_ZAI_GRD_FLG"]        = $tmp[++$tmpCnt];
                        $data["SINZOKU_FUTAGO_NAME"]        = $tmp[++$tmpCnt];
                        $data["SINZOKU_FUTAGO_ZOKUGARA"]    = $tmp[++$tmpCnt];
                        $data["INTERVIEW_REMARK"]           = $tmp[++$tmpCnt];
                        $data["INTERVIEW_VALUE"]            = $tmp[++$tmpCnt];
                        $data["REMARK1"]                    = $tmp[++$tmpCnt];
                        $data[$this->lastColumn]            = $tmp[++$tmpCnt];

                    //調査書
                    } elseif ($this->field["DATADIV"] == "2") {
                        $data["ENTEXAMYEAR"]            = $tmp[0];
                        $data["APPLICANTDIV"]           = $tmp[1];
                        $data["DATADIV"]                = $tmp[2];
                        $data["EXAMNO"]                 = $tmp[3];

                        $tmpCnt = 3;
                        foreach ($class_array as $classcd => $label) {
                            $data["CLASSCD".$classcd]       = $tmp[++$tmpCnt];
                        }
                        $data["TOTAL_ALL"]              = $tmp[++$tmpCnt];
                        $data["AVERAGE_ALL"]            = $tmp[++$tmpCnt];
                        $data["ABSENCE_DAYS"]           = $tmp[++$tmpCnt];
                        $data["ABSENCE_REMARK"]         = $tmp[++$tmpCnt];
                        $data["ABSENCE_DAYS2"]          = $tmp[++$tmpCnt];
                        $data["ABSENCE_REMARK2"]        = $tmp[++$tmpCnt];
                        $data["ABSENCE_DAYS3"]          = $tmp[++$tmpCnt];
                        $data["ABSENCE_REMARK3"]        = $tmp[++$tmpCnt];
                        $data["JITURYOKU_KOKUGO"]       = $tmp[++$tmpCnt];
                        $data["JITURYOKU_EIGO"]         = $tmp[++$tmpCnt];
                        $data["JITURYOKU_SUUGAKU"]      = $tmp[++$tmpCnt];
                        $data["JITURYOKU_TOTAL"]        = $tmp[++$tmpCnt];
                        $data["MOGI_HENSATI"]           = $tmp[++$tmpCnt];
                        $data[$this->lastColumn]        = $tmp[++$tmpCnt];
                    }

                    //項目数によるファイルチェック
                    if ($checkcount !=get_count($data)) {
                        $this->warning = "CSVファイルを確認して下さい。\\nフォーマットが一致していません。";
                        return;
                    }

                    //エラーチェック
                    if ($this->errCheck($db, $data, $checkdata, $line)) {
                        foreach ($checkdata as $key => $val) {
                            //日付を変換してセット
                            if ($val[2] == "DATE") {
                                if ($key == "FS_DAY") {
                                    $data[$key] = $data[$key]."/01";
                                }
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

        //受験番号範囲チェック用
        $checkNumber = array();
        if ($this->field["DATADIV"] == "1") {
            $checkNumber = $this->checkNumber();
        }

        foreach ($checkdata as $key => $val) {
            //必須項目チェック
            if ($val[1] == "HISSU" && !strlen(trim($data[$key]))) {
                $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[3]);
            }

            //数値チェック
            if (($val[2] == "INT" || $val[2] == "INT2" || $val[2] == "FLOAT") && $data[$key] != "" && !is_numeric($data[$key])) {
                $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, $val[3]);
            }
            //チェック用にゼロ埋め
            if ($val[2] == "INT2" && $data[$key] != "" && is_numeric($data[$key])) {
                $data[$key] = sprintf("%0{$val[0]}d", $data[$key]);
            }

            //存在チェック
            $array = array("ENTEXAMYEAR","APPLICANTDIV","DATADIV");
            if (in_array($key, $array)) {
                if (($key == "ENTEXAMYEAR" && $this->ObjYear != $data[$key]) || ($key != "ENTEXAMYEAR" && $this->field["$key"] != $data[$key])) {
                    $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[3]);
                }
            }
            if (in_array($key, array("DAI1_COURSECODE","DAI2_COURSECODE")) && strlen($data[$key])) {
                $dai = preg_split("{_}", $key);
                if (0 == $db->getOne(knjl014gQuery::examcoursecdCntSql($data["ENTEXAMYEAR"], $data["APPLICANTDIV"], $data["TESTDIV"], 1, $data[$dai[0]."_MAJORCD"], $data[$key]))) {
                    $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[3]);
                }
            }
            if (in_array($key, array("SHDIV","DAI2_SENPEIDIV")) && strlen($data[$key]) && 0 == $db->getOne(knjl014gQuery::nameCntSql("L006", $data[$key]))) {
                $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[3]);
            }
            if ($key == "SEX" && strlen($data[$key]) && 0 == $db->getOne(knjl014gQuery::nameCntSql("Z002", $data[$key]))) {
                $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[3]);
            }
            if ($key == 'FS_CD' && strlen($data[$key]) && 0 == $db->getOne(knjl014gQuery::fsCdCntSql($data[$key]))) {
                $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[3]);
            }
            if ($key == 'FS_GRDDIV' && strlen($data[$key]) && 0 == $db->getOne(knjl014gQuery::nameCntSql("L016", $data[$key]))) {
                $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[3]);
            }
            if (in_array($key, array("RELATIONSHIP","SINZOKU_ZOKUGARA","SINZOKU_FUTAGO_ZOKUGARA")) && strlen($data[$key]) && 0 == $db->getOne(knjl014gQuery::nameCntSql("H201", $data[$key]))) {
                $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[3]);
            }
            if ($key == 'JIZEN_SOUDAN_CD' && strlen($data[$key]) && 0 == $db->getOne(knjl014gQuery::nameCntSql("L032", $data[$key]))) {
                $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[3]);
            }
            if (in_array($key, array("CLUB_CD","HOPE_CLUB_CD")) && strlen($data[$key]) && 0 == $db->getOne(knjl014gQuery::nameCntSql("L037", $data[$key]))) {
                $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[3]);
            }
            if ($key == 'CLUB_RANK' && strlen($data[$key]) && 0 == $db->getOne(knjl014gQuery::nameCntSql("L025", $data[$key]))) {
                $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[3]);
            }
            if ($key == 'SINZOKU_ZAI_GRD_FLG' && strlen($data[$key]) && 0 == $db->getOne(knjl014gQuery::nameCntSql("L038", $data[$key]))) {
                $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[3]);
            }
            if ($key == 'INTERVIEW_VALUE' && strlen($data[$key]) && 0 == $db->getOne(knjl014gQuery::nameCntSql("L027", $data[$key]))) {
                $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[3]);
            }

            //日付チェック
            if ($val[2] == "DATE" && $data[$key] != "") {
                $chkdata = $data[$key];
                if ($key == "FS_DAY") {
                    $chkdata = $data[$key]."/01";
                }
                if ($this->dateRep($chkdata) == "") {
                    $returnFlg = $this->errSet($otherErrFlg, $otherErrMsg, "※その他:", $otherSep, $val[3]);
                }
            }

            //文字数チェック
            if (strlen($data[$key]) > $val[0]) {
                $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[3]);
            }

            //文字数チェック（整数部：2桁、小数部：1桁まで）
            if (($val[2] == "FLOAT") && $data[$key] != "" && is_numeric($data[$key])) {
                list($int, $decimal) = explode('.', $data[$key]);
                if (strlen($int) > 2) {
                    $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[3]."の整数部");
                } elseif (strlen($decimal) > 1) {
                    $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[3]."の小数部");
                }
            }
        }

        //受験番号範囲チェック
        $checkNumber_err_cnt = 0;
        if ($this->field["DATADIV"] == "1") {
            if ($this->isKasiwara == "1") {
                if ($data["TESTDIV"] && $data["SHDIV"] && $data["EXAMNO"]) {
                    foreach ($checkNumber as $key2 => $val2) {
                        if ($data["TESTDIV"] == $val2["TESTDIV"] && $data["SHDIV"] == $val2["SHDIV"] && ($data["DAI1_COURSECODE"] == $val2["EXAMCOURSE"] || "NO" == $val2["EXAMCOURSE"])) {
                            if ($data["EXAMNO"] < $val2["MIN_NO"] || $data["EXAMNO"] > $val2["MAX_NO"]) {
                                $checkNumber_err_cnt++;
                            } else {
                                break;
                            }
                        }
                    }
                }
            } else {//isKeiai
                if ($data["TESTDIV"] && $data["SHDIV"] && $data["EXAMNO"]) {
                    foreach ($checkNumber as $key2 => $val2) {
                        if ($data["TESTDIV"] == $val2["TESTDIV"] && $data["SHDIV"] == $val2["SHDIV"]) {
                            if ($data["EXAMNO"] >= $val2["MIN_NO"] && $data["EXAMNO"] <= $val2["MAX_NO"]) {
                            } elseif (strlen($val2["MIN_NO2"]) && $data["EXAMNO"] >= $val2["MIN_NO2"] && $data["EXAMNO"] <= $val2["MAX_NO2"]) {
                            } else {
                                $checkNumber_err_cnt++;
                            }
                        }
                    }
                }
            }
        }
        if ($checkNumber_err_cnt > 0) {
            $returnFlg = $this->errSet($otherErrFlg, $otherErrMsg, "※その他:", $otherSep, "受験番号範囲不可");
        }

        if ($this->field["DATADIV"] == "2") {
            $base_cnt = $db->getOne(knjl014gQuery::cntEntexamApplicantbaseDat($this, $data));
            //存在チェック -- ENTEXAM_APPLICANTBASE_DAT
            if ($base_cnt == 0) {
                $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, "願書データ");
            }
        }

        if (!$returnFlg) {
            $check_error = $hissuErrMsg.$numericErrMsg.$lengthErrMsg.$existenceErrMsg.$otherErrMsg;
            if (strlen($check_error) > 120) {
                $check_error = mb_strcut($check_error, 0, 115);
                $check_error .= "..他";
            }
            knjl014gQuery::insertQueryErr($db, $line, $check_error);
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
    public function checkNumber()
    {
        $check = array();
        if ($this->isKasiwara == "1") {
            $check[] = array("TESTDIV" => "1", "SHDIV" => "1", "EXAMCOURSE" => "3000", "MIN_NO" => "0001", "MAX_NO" => "0199"); //キャリアアシスト
            $check[] = array("TESTDIV" => "1", "SHDIV" => "1", "EXAMCOURSE" => "NO",   "MIN_NO" => "0201", "MAX_NO" => "0599"); //キャリアアシスト以外
            $check[] = array("TESTDIV" => "2", "SHDIV" => "1", "EXAMCOURSE" => "NO",   "MIN_NO" => "0601", "MAX_NO" => "0699");
            $check[] = array("TESTDIV" => "3", "SHDIV" => "1", "EXAMCOURSE" => "NO",   "MIN_NO" => "0701", "MAX_NO" => "0799");
            $check[] = array("TESTDIV" => "1", "SHDIV" => "2", "EXAMCOURSE" => "NO",   "MIN_NO" => "1001", "MAX_NO" => "2999");
            $check[] = array("TESTDIV" => "2", "SHDIV" => "2", "EXAMCOURSE" => "NO",   "MIN_NO" => "3001", "MAX_NO" => "3999");
        } else {
            $check[] = array("TESTDIV" => "1", "SHDIV" => "1", "MIN_NO" => "1001", "MAX_NO" => "1999", "MIN_NO2" => "4001", "MAX_NO2" => "4999");
            $check[] = array("TESTDIV" => "1", "SHDIV" => "2", "MIN_NO" => "2001", "MAX_NO" => "2999", "MIN_NO2" => "5001", "MAX_NO2" => "5999");
            $check[] = array("TESTDIV" => "2", "SHDIV" => "1", "MIN_NO" => "6001", "MAX_NO" => "6999");
            $check[] = array("TESTDIV" => "2", "SHDIV" => "2", "MIN_NO" => "7001", "MAX_NO" => "7999");
            $check[] = array("TESTDIV" => "3", "SHDIV" => "1", "MIN_NO" => "8001", "MAX_NO" => "8999");
            $check[] = array("TESTDIV" => "3", "SHDIV" => "2", "MIN_NO" => "8001", "MAX_NO" => "8999");
        }
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
        $class_array = array();

        //エラー項目
        $header_err = array("MSGROW"        => "行番号",
                            "MSGREMARK"     => "メッセージ");

        /***********/
        /*  願 書  */
        /***********/
        if ($this->field["DATADIV"] == "1") {
            /**************/
            /*  ヘッダー  */
            /**************/
            $header["ENTEXAMYEAR"]              = "※入試年度";
            $header["APPLICANTDIV"]             = "※入試制度";
            $header["DATADIV"]                  = "※データ種類";
            $header["EXAMNO"]                   = "※受験番号";
            $header["RECEPTDATE"]               = "※受付日付";
            $header["TESTDIV"]                  = "※入試区分";
            $header["SHDIV"]                    = "※専併区分";
            $header["DAI1_MAJORCD"]             = "※第１志望学科";
            $header["DAI1_COURSECODE"]          = "※第１志望受験コース";
            $header["DAI1_COURSE_NAME"]         = "第１志望コース名";
            $header["DAI2_MAJORCD"]             = "第２志望学科";
            $header["DAI2_COURSECODE"]          = "第２志望受験コース";
            $header["DAI2_COURSE_NAME"]         = "第２志望コース名";
            //柏原のみ
            if ($this->isKasiwara == "1") {
                $header["DAI2_SENPEIDIV"]           = "第２志望専併区分";
                $header["HOPE_CLUB_CD"]             = "志望クラブコード";
                $header["HOPE_CLUB_NAME"]           = "志望クラブ名";
            }
            $header["NAME"]                     = "※氏名";
            $header["NAME_KANA"]                = "※ふりがな";
            $header["SEX"]                      = "※性別";
            $header["BIRTHDAY"]                 = "生年月日";
            $header["ZIPCD"]                    = "郵便番号";
            $header["ADDRESS1"]                 = "住所";
            $header["ADDRESS2"]                 = "方書";
            $header["TELNO"]                    = "電話番号";
            $header["FS_CD"]                    = "出身学校コード";
            $header["FS_NAME"]                  = "出身学校名";
            $header["FS_DAY"]                   = "卒業年月";
            $header["FS_GRDDIV"]                = "卒業区分";
            $header["GNAME"]                    = "保護者氏名";
            $header["GKANA"]                    = "保護者ふりがな";
            $header["GZIPCD"]                   = "保護者郵便番号";
            $header["GADDRESS1"]                = "保護者住所";
            $header["GADDRESS2"]                = "保護者方書";
            $header["GTELNO"]                   = "保護者電話番号";
            $header["RELATIONSHIP"]             = "保護者続柄";
            $header["JIZEN_SOUDAN_CD"]          = "事前相談情報";
            $header["JIZEN_SOUDAN_TEXT"]        = "その他";
            $header["CLUB_CD"]                  = "クラブコード";
            $header["CLUB_NAME"]                = "クラブ名";
            $header["CLUB_RANK"]                = "クラブランク";
            $header["SINZOKU_NAME"]             = "親族氏名";
            $header["SINZOKU_KYUSEI"]           = "親族旧姓";
            $header["SINZOKU_ZOKUGARA"]         = "親族続柄";
            $header["SINZOKU_GAKKOUMEI"]        = "親族学校名";
            $header["SINZOKU_GAKKA"]            = "親族学科・科";
            $header["SINZOKU_ZAI_GRD_YEAR"]     = "親族在学・卒業年";
            $header["SINZOKU_ZAI_GRD_FLG"]      = "親族在学・卒業区分";
            $header["SINZOKU_FUTAGO_NAME"]      = "双生児氏名";
            $header["SINZOKU_FUTAGO_ZOKUGARA"]  = "双生児続柄";
            $header["INTERVIEW_REMARK"]         = "面接情報";
            $header["INTERVIEW_VALUE"]          = "面接評価";
            $header["REMARK1"]                  = "気づき情報";
            $header[$this->lastColumn]          = $this->lastColumn;

            /****************/
            /*  ヘッダー例  */
            /****************/
            //専併区分
            $shdiv = implode(' ', $db->getCol(knjl014gQuery::getHeaderReiNameMst($this->ObjYear, "L006", "NAME1")));
            //性別
            $sex = implode(' ', $db->getCol(knjl014gQuery::getHeaderReiNameMst($this->ObjYear, "Z002", "NAME1")));
            //卒業区分
            $fs_grddiv = implode(' ', $db->getCol(knjl014gQuery::getHeaderReiNameMst($this->ObjYear, "L016", "NAME1")));
            //親族在学・卒業区分
            $sinzoku_flg = implode(' ', $db->getCol(knjl014gQuery::getHeaderReiNameMst($this->ObjYear, "L038", "NAME1")));
            //面接評価
            $int_val = implode(' ', $db->getCol(knjl014gQuery::getHeaderReiNameMst($this->ObjYear, "L027", "NAME1")));

            $header_rei["ENTEXAMYEAR"]              = $this->ObjYear;
            $header_rei["APPLICANTDIV"]             = $this->field["APPLICANTDIV"];
            $header_rei["DATADIV"]                  = $this->field["DATADIV"];
            $header_rei["EXAMNO"]                   = "1234";
            $header_rei["RECEPTDATE"]               = str_replace("-", "/", CTRL_DATE);
            $header_rei["TESTDIV"]                  = "1";
            $header_rei["SHDIV"]                    = $shdiv;
            $header_rei["DAI1_MAJORCD"]             = "123";
            $header_rei["DAI1_COURSECODE"]          = "1234";
            $header_rei["DAI1_COURSE_NAME"]         = "入力の必要なし";     //表示のみ
            $header_rei["DAI2_MAJORCD"]             = "123";
            $header_rei["DAI2_COURSECODE"]          = "1234";
            $header_rei["DAI2_COURSE_NAME"]         = "入力の必要なし";     //表示のみ
            //柏原のみ
            if ($this->isKasiwara == "1") {
                $header_rei["DAI2_SENPEIDIV"]           = $shdiv;
                $header_rei["HOPE_CLUB_CD"]             = "12";
                $header_rei["HOPE_CLUB_NAME"]           = "入力の必要なし";     //表示のみ
            }
            $header_rei["NAME"]                     = "アルプ太郎";
            $header_rei["NAME_KANA"]                = "あるぷたろう";
            $header_rei["SEX"]                      = $sex;
            $header_rei["BIRTHDAY"]                 = date('Y/m/d', strtotime(CTRL_DATE." -15 years"));
            $header_rei["ZIPCD"]                    = "123-4567";
            $header_rei["ADDRESS1"]                 = "全角30文字";
            $header_rei["ADDRESS2"]                 = "全角30文字";
            $header_rei["TELNO"]                    = "12-3456-7890";
            $header_rei["FS_CD"]                    = "1234567";
            $header_rei["FS_NAME"]                  = "入力の必要なし";     //表示のみ
            $header_rei["FS_DAY"]                   = CTRL_YEAR."/03";
            $header_rei["FS_GRDDIV"]                = $fs_grddiv;
            $header_rei["GNAME"]                    = "アルプ父";
            $header_rei["GKANA"]                    = "あるぷちち";
            $header_rei["GZIPCD"]                   = "123-4567";
            $header_rei["GADDRESS1"]                = "全角30文字";
            $header_rei["GADDRESS2"]                = "全角30文字";
            $header_rei["GTELNO"]                   = "123-456-7890";
            $header_rei["RELATIONSHIP"]             = "12";
            $header_rei["JIZEN_SOUDAN_CD"]          = "1";
            $header_rei["JIZEN_SOUDAN_TEXT"]        = "全角5文字";
            $header_rei["CLUB_CD"]                  = "12";
            $header_rei["CLUB_NAME"]                = "入力の必要なし";     //表示のみ
            $header_rei["CLUB_RANK"]                = "1";
            $header_rei["SINZOKU_NAME"]             = "アルプ姉";
            $header_rei["SINZOKU_KYUSEI"]           = "旧姓";
            $header_rei["SINZOKU_ZOKUGARA"]         = "12";
            $header_rei["SINZOKU_GAKKOUMEI"]        = "学校";
            $header_rei["SINZOKU_GAKKA"]            = "学科";
            $header_rei["SINZOKU_ZAI_GRD_YEAR"]     = CTRL_YEAR;
            $header_rei["SINZOKU_ZAI_GRD_FLG"]      = $sinzoku_flg;
            $header_rei["SINZOKU_FUTAGO_NAME"]      = "アルプ弟";
            $header_rei["SINZOKU_FUTAGO_ZOKUGARA"]  = "12";
            $header_rei["INTERVIEW_REMARK"]         = "全角60文字";
            $header_rei["INTERVIEW_VALUE"]          = $int_val;
            $header_rei["REMARK1"]                  = "全角60文字";
            $header_rei[$this->lastColumn]          = $this->lastColumn;
        }

        /**************/
        /*  調 査 書  */
        /**************/
        if ($this->field["DATADIV"] == "2") {
            //教科
            $result = $db->query(knjl014gQuery::getNameMstAll($this->ObjYear, "L008"));
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
                $header["CLASSCD".$key]     = "内申_".$label;
            }
            $header["TOTAL_ALL"]            = "内申_合計";
            $header["AVERAGE_ALL"]          = "内申_平均";
            $header["ABSENCE_DAYS"]         = "1年生欠席日数";
            $header["ABSENCE_REMARK"]       = "1年生欠席理由";
            $header["ABSENCE_DAYS2"]        = "2年生欠席日数";
            $header["ABSENCE_REMARK2"]      = "2年生欠席理由";
            $header["ABSENCE_DAYS3"]        = "3年生欠席日数";
            $header["ABSENCE_REMARK3"]      = "3年生欠席理由";
            $header["JITURYOKU_KOKUGO"]     = "実力テスト_国語";
            $header["JITURYOKU_EIGO"]       = "実力テスト_英語";
            $header["JITURYOKU_SUUGAKU"]    = "実力テスト_数学";
            $header["JITURYOKU_TOTAL"]      = "実力テスト_合計";
            $header["MOGI_HENSATI"]         = "模擬試験_偏差値";
            $header[$this->lastColumn]      = $this->lastColumn;

            /****************/
            /*  ヘッダー例  */
            /****************/
            $header_rei["ENTEXAMYEAR"]          = $this->ObjYear;
            $header_rei["APPLICANTDIV"]         = $this->field["APPLICANTDIV"];
            $header_rei["DATADIV"]              = $this->field["DATADIV"];
            $header_rei["EXAMNO"]               = "1234";
            foreach ($class_array as $key => $label) {
                $header_rei["CLASSCD".$key]     = "12";
            }
            $header_rei["TOTAL_ALL"]            = "123";
            $header_rei["AVERAGE_ALL"]          = "12.3";
            $header_rei["ABSENCE_DAYS"]         = "123";
            $header_rei["ABSENCE_REMARK"]       = "全角10文字";
            $header_rei["ABSENCE_DAYS2"]        = "123";
            $header_rei["ABSENCE_REMARK2"]      = "全角10文字";
            $header_rei["ABSENCE_DAYS3"]        = "123";
            $header_rei["ABSENCE_REMARK3"]      = "全角10文字";
            $header_rei["JITURYOKU_KOKUGO"]     = "123";
            $header_rei["JITURYOKU_EIGO"]       = "123";
            $header_rei["JITURYOKU_SUUGAKU"]    = "123";
            $header_rei["JITURYOKU_TOTAL"]      = "123";
            $header_rei["MOGI_HENSATI"]         = "12.3";
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
                $query = knjl014gQuery::selectCsvErrQuery();
            } elseif ($this->field["OUTPUT"] == "4") {
                //データ
                if ($this->field["DATADIV"] == "1") {
                    //願書
                    $query = knjl014gQuery::selectMainQuery1($this);
                } elseif ($this->field["DATADIV"] == "2") {
                    //調査書
                    $query = knjl014gQuery::selectMainQuery2($this, $class_array);
                }
            }

            if ($this->field["DATADIV"] == "1") {
                //日付変換項目一覧
                $date_array = array("RECEPTDATE","BIRTHDAY","FS_DAY");
            }

            //SQL文発行
            if ($query) {
                $result = $db->query($query);
                while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    if ($this->field["OUTPUT"] == "4") {     //データ
                        if ($this->field["DATADIV"] == "1") {   //願書
                            //日付変換
                            foreach ($date_array as $key) {
                                $row[$key] = str_replace("-", "/", $row[$key]);
                                if ($key == "FS_DAY") {
                                    $row[$key] = substr($row[$key], 0, 7);
                                }
                            }
                        } elseif ($this->field["DATADIV"] == "2") {    //調査書
                            if ($row["AVERAGE_ALL"] > 0) {
                                $row["AVERAGE_ALL"] = round($row["AVERAGE_ALL"]*10)/10;
                            }
                            if ($row["MOGI_HENSATI"] > 0) {
                                $row["MOGI_HENSATI"] = round($row["MOGI_HENSATI"]*10)/10;
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
        $AppName = $db->getOne(knjl014gQuery::getNameMst($this->ObjYear, "L003", $this->field["APPLICANTDIV"]));
        //データ種類
        $datadiv = array("1" => "願書", "2" => "調査書");
        $DataDivName = $datadiv[$this->field["DATADIV"]];
        //ファイル名
        $title = $this->ObjYear.'年度 '.$AppName.' '.$DataDivName.'データ';

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
