<?php

require_once('for_php7.php');
class knja040mModel extends Model
{
    public $cmd;
    public $file;
    public $data_arr = array();
    public $field = array();
    public $lastColumn;//CSVの最後の列の値

    public function knja040mModel()
    {
    }

    public function init()
    {
        //プロパティファイル
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        //初期化
        if (VARS::get("PROGRAMID") != "") {
            $this->field = array();
            $this->field["HEADERCHECK"] = 1;
        }

        $this->cmd = VARS::request("cmd");

        $this->field = array("DATADIV"              =>  VARS::post("DATADIV"),              //対象データ
                             "OUTPUT"               =>  VARS::post("OUTPUT"),               //出力取込種別
                             "EXE_YEAR"             =>  VARS::post("EXE_YEAR"),             //処理年度
                             "GUARD_ADDR_FLG"       =>  VARS::post("GUARD_ADDR_FLG"),       //住所2出力
                             "GUARD_ISSUEDATE"      =>  VARS::post("GUARD_ISSUEDATE"),      //住所開始日
                             "GUARD_EXPIREDATE"     =>  VARS::post("GUARD_EXPIREDATE"),     //住所終了日
                             "HEADERCHECK"          =>  VARS::post("HEADERCHECK")           //ヘッダ有
                             );

        //初期化
        if (VARS::get("PROGRAMID") != "") {
            $this->field["HEADERCHECK"] = 1;
        }

        $this->file = VARS::file("FILE");
 
        $this->lastColumn = 'DUMMY';
        if ($this->Properties["csv_LastColumn"] != '') {
            $this->lastColumn = $this->Properties["csv_LastColumn"];
        }
    }

    public function &getUpdateModel()
    {
        if ($this->field["DATADIV"] == 2) {
            $this->getExecModel();
        } else {
            //ファイルの文字列の取得
            $this->data_arr = $this->getFileString();

            if ($this->warning) {
                return false;
            }

            //データの整合性チェック
            if ($this->isDataMatched($this->data_arr)) {
                $errFlg = $this->checkCsvErrQuery(PROGRAMID);
                if ($errFlg) {
                    knja040mQuery::insertQuery($this);
                    $this->setMessage("MSG202", "更新件数".get_count($this->data_arr)."件");
                }
            }
        }
    }

    public function getFileString()
    {
        $temp_arr = array();
        $examLen = $this->Properties["examnoLen"] ? $this->Properties["examnoLen"] : "5";
        if (!is_uploaded_file($this->file["tmp_name"])) {       //HTTP POSTによりアップロードされたファイルかどうかを調べる
            $this->warning = "ファイル名が不正です。";
        } else {
            //エラーデータの削除
            $db = Query::dbCheckOut();
            $query = $this->deleteQueryErr(PROGRAMID);
            $db->query($query);

            //学校取得
            $query = knja040mQuery::checkSchool();
            $schoolDiv = $db->getRow($query, DB_FETCHMODE_ASSOC);

            //CSVフォーマットType
            //     1：学校コード、卒業年月日
            // 1以外：学校コード、学校名、卒業年月日
            $query = knja040mQuery::getCsvFormatType();
            $formatType = $db->getOne($query);

            Query::dbCheckIn($db);

            $checkdata = array();
            $checkdata["ENTERYEAR"]             = array(4,  "入学処理年度");
            $checkdata["SCHREGNO"]              = array(8,  "学籍番号");
            $checkdata["ENT_DIV"]               = array(1,  "入学区分");
            $checkdata["GRADE"]                 = array(2,  "年");
            $checkdata["HR_CLASS"]              = array(3,  "組");
            $checkdata["ATTENDNO"]              = array(3,  "出席番号");
            $checkdata["INOUTCD"]               = array(1,  "内外区分");
            $checkdata["COURSECD"]              = array(1,  "課程コード");
            $checkdata["MAJORCD"]               = array(3,  "学科コード");
            $checkdata["COURSECODE"]            = array(4,  "コースコード");
            $checkdata["NAME"]                  = array(120,"生徒氏名");
            $checkdata["NAME_KANA"]             = array(240,"生徒氏名かな");
            $checkdata["BIRTHDAY"]              = array(10, "生年月日");
            $checkdata["SEX"]                   = array(1,  "性別");
            if ($formatType == "1") {
                $checkdata["FINSCHOOLCD"]        = array(($this->Properties["useFinschoolcdFieldSize"] == '12' ? 12 : 7),"中学校コード");
                $checkdata["FINSCHOOLGRADDATE"]  = array(10,"中学卒業年月日");
            } else {
                $checkdata["FINSCHOOLCD"]        = array(($this->Properties["useFinschoolcdFieldSize"] == '12' ? 12 : 7),"中学校コード");
                $checkdata["FINSCHOOLNAME"]      = array(306,"出身学校名");
                $checkdata["FINSCHOOLGRADDATE"]  = array(10,"中学卒業年月日");
            }
            $checkdata["ZIPCD"]                 = array(8,  "郵便番号");
            $checkdata["ADDRESS1"]              = array(150, "住所1");
            $checkdata["ADDRESS2"]              = array(150, "住所2");
            $checkdata["TELNO"]                 = array(14, "電話番号");
            $checkdata["FAXNO"]                 = array(14, "FAX番号");
            $checkdata["EMAIL"]                 = array(50, "E-MAIL");
            $checkdata["EMERGENCYCALL"]         = array(60, "急用連絡先");
            $checkdata["EMERGENCYTELNO"]        = array(14, "急用電話番号");
            $checkdata["SCALASHIPDIV"]          = array(2,  "スカラシップ");
            $checkdata["CURRICULUM_YEAR"]       = array(4,  "課程入学年度");
            $checkdata["AREACD"]                = array(2,  "地区コード");
            $checkdata["EXAMNO"]                = array($examLen, "受験番号");
            if ($this->Properties["UseMusyouKaisu"] == "1") {
                $checkdata["MUSYOU_KAISU"]          = array(1,  "無償回数");
            }
            $checkdata["TESTDIV"]               = array(2,"入試区分");
            $checkdata[$this->lastColumn]       = array(10,$this->lastColumn);

            //エラーデータの削除
            $db = Query::dbCheckOut();
            $db->autoCommit(false);

            $fp = fopen($this->file["tmp_name"], "r");
            $i  = 0;
            $line = 1;  //行数
            $tmp_schregno = array();
            $header = false;

            //取得主キー
            $primaryKey1 = array();
            $primaryKey2 = array();
            $primaryKey3 = array();
            $primaryKey4 = array();
            $duplicateCsv1 = array();
            $duplicateCsv2 = array();
            $duplicateCsv3 = array();
            $duplicateCsv4 = array();

            $primaryKey1 = $this->getPrimaryKey('FRESHMAN_DAT');
            $primaryKey2 = $this->getPrimaryKey('SCHREG_BASE_MST');
            $primaryKey3 = $this->getPrimaryKey('SCHREG_ENT_GRD_HIST_DAT');
            $primaryKey4 = $this->getPrimaryKey('SCHREG_BASE_DETAIL_MST');
            while (!feof($fp)) {
                $buffer = fgets($fp, 4096);
                if (trim($buffer) != '') {
                    if (!$header && $this->field["HEADERCHECK"] == 1) {
                        $header = true;
                        $line++;
                        continue;
                    }
                    //文字コードを判別、改行コードを削除
                    $buffer = trim(i18n_convert($buffer, i18n_http_output(), "SJIS-win"));
                    $tmp    = common::csv2array($buffer);
                    $dataCnt = 0;
                    $data = array();
                    $data["ENTERYEAR"]              = $tmp[$dataCnt++];
                    $data["SCHREGNO"]               = (strlen(trim($tmp[$dataCnt]))) ? sprintf("%08d", $tmp[$dataCnt]) : "";
                    $dataCnt++;
                    $data["ENT_DIV"]                = $tmp[$dataCnt++];
                    $data["GRADE"]                  = (strlen(trim($tmp[$dataCnt]))) ? sprintf("%02s", $tmp[$dataCnt]) : "";
                    $dataCnt++;

                    //校種取得
                    $getSK = $db->getOne(knja040mQuery::getSchoolKind($this, $data["GRADE"]));
                    $data["SCHOOL_KIND"] = ($getSK) ? $getSK : SCHOOLKIND;

                    $data["HR_CLASS"]               = (strlen(trim($tmp[$dataCnt]))) ? sprintf("%03s", $tmp[$dataCnt]) : "";
                    $dataCnt++;
                    $data["ATTENDNO"]               = (strlen(trim($tmp[$dataCnt]))) ? sprintf("%03d", $tmp[$dataCnt]) : "";
                    $dataCnt++;
                    $data["INOUTCD"]                = $tmp[$dataCnt++];
                    $data["COURSECD"]               = $tmp[$dataCnt++];
                    $data["MAJORCD"]                = (strlen(trim($tmp[$dataCnt]))) ? sprintf("%03d", $tmp[$dataCnt]) : "";
                    $dataCnt++;
                    $data["COURSECODE"]             = (strlen(trim($tmp[$dataCnt]))) ? sprintf("%04d", $tmp[$dataCnt]) : "";
                    $dataCnt++;
                    $data["NAME"]                   = $tmp[$dataCnt++];
                    $data["NAME_KANA"]              = $tmp[$dataCnt++];
                    $data["BIRTHDAY"]               = $tmp[$dataCnt++];
                    $data["SEX"]                    = $tmp[$dataCnt++];
                    if ($formatType == "1") {
                        $data["FINSCHOOLCD"]          = (strlen(trim($tmp[$dataCnt]))) ? sprintf("%0".($this->Properties["useFinschoolcdFieldSize"] == '12' ? "12" : "7")."s", $tmp[$dataCnt]) : "";
                        $dataCnt++;
                        $data["FINSCHOOLGRADDATE"]    = $tmp[$dataCnt++];
                    } else {
                        if ($data["SCHOOL_KIND"] == "P" || $data["SCHOOL_KIND"] == "K") {
                            //学校コード読み飛ばす
                            $dataCnt++;
                            //学校名
                            $data["FINSCHOOLNAME"]        = $tmp[$dataCnt++];
                            //卒業年月日読み飛ばす
                            $dataCnt++;
                        } else {
                            //学校コード
                            $data["FINSCHOOLCD"]          = (strlen(trim($tmp[$dataCnt]))) ? sprintf("%0".($this->Properties["useFinschoolcdFieldSize"] == '12' ? "12" : "7")."s", $tmp[$dataCnt]) : "";
                            $dataCnt++;
                            //学校名読み飛ばす
                            $dataCnt++;
                            //卒業年月日
                            $data["FINSCHOOLGRADDATE"]    = $tmp[$dataCnt++];
                        }
                    }
                    $data["ZIPCD"]                  = $tmp[$dataCnt++];
                    $data["ADDRESS1"]               = $tmp[$dataCnt++];
                    $data["ADDRESS2"]               = $tmp[$dataCnt++];
                    $data["TELNO"]                  = $tmp[$dataCnt++];
                    $data["FAXNO"]                  = $tmp[$dataCnt++];
                    $data["EMAIL"]                  = $tmp[$dataCnt++];
                    $data["EMERGENCYCALL"]          = $tmp[$dataCnt++];
                    $data["EMERGENCYTELNO"]         = $tmp[$dataCnt++];
                    $data["SCALASHIPDIV"]           = (strlen(trim($tmp[$dataCnt]))) ? sprintf("%02d", $tmp[$dataCnt]) : "";
                    $dataCnt++;
                    $data["CURRICULUM_YEAR"]        = $tmp[$dataCnt++];
                    $data["AREACD"]                 = $tmp[$dataCnt++];
                    $data["EXAMNO"]                 = $tmp[$dataCnt++];
                    if ($this->Properties["useMusyouKaisu"] == "1") {
                        $data["MUSYOU_KAISU"]         = $tmp[$dataCnt++];
                    }
                    $data["TESTDIV"]              = $tmp[$dataCnt++];
                    $data["BASE_SEQ"]             = "";
                    $data[$this->lastColumn]      = $tmp[$dataCnt++];

                    //エラーチェック
                    if ($this->errCheck($db, $data, $checkdata, $line, $tmp_schregno, $schoolDiv["NAME1"], $duplicateCsv1, $duplicateCsv2, $duplicateCsv3, $duplicateCsv4, $primaryKey1, $primaryKey2, $primaryKey3, $primaryKey4)) {
                        $temp_arr[$i] = $data;
                        $i++;
                        $tmp_schregno[] = $data["SCHREGNO"];
                    }
                    foreach ($primaryKey1 as $key => $val) {
                        $duplicateCsv1[$line][$val] = $data[$val];
                    }
                    foreach ($primaryKey2 as $key => $val) {
                        $duplicateCsv2[$line][$val] = $data[$val];
                    }
                    foreach ($primaryKey3 as $key => $val) {
                        $duplicateCsv3[$line][$val] = $data[$val];
                    }
                    foreach ($primaryKey4 as $key => $val) {
                        $duplicateCsv4[$line][$val] = $data[$val];
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

    //データの整合性チェック
    public function isDataMatched(&$data_arr)
    {
        //５千件を超えたら制限エラー
        if (get_count($data_arr) > 5000) {
            $this->setWarning("MSG915", "処理件数を５千件以下にして下さい 処理件数：".get_count($data_arr)."件");
            return false;
        }
        return true;
    }

    public function errCheck($db, $data, $data_each, $line, $tmp_schregno, $schoolDiv, $duplicateCsv1, $duplicateCsv2, $duplicateCsv3, $duplicateCsv4, $primaryKey1, $primaryKey2, $primaryKey3, $primaryKey4)
    {
        $returnFlg            = true;
        $hissuErrFlg          = false;    //必須エラーフラグ
        $existenceErrFlg      = false;    //存在エラーフラグ
        $numericErrFlg        = false;    //数字エラーフラグ
        $lengthErrFlg         = false;    //レングスエラーフラグ
        $valuesErrFlg         = false;    //値エラーフラグ
        $duplicateCsvErrFlg   = false;    //重複(CSV)エラーフラグ
        $duplicateEntryErrFlg = false;    //重複(登録済)エラーフラグ
        $lastColErrFlg        = false;    //最終列文字エラーフラグ
        $dateFormatErrFlg     = false;    //日付(フォーマット)エラーフラグ

        $hissuErrMsg          = "";       //必須エラーメッセージ
        $existenceErrMsg      = "";       //存在エラーメッセージ
        $numericErrMsg        = "";       //数字エラーメッセージ
        $lengthErrMsg         = "";       //レングスエラーメッセージ
        $valuesErrMsg         = "";       //値エラーメッセージ
        $duplicateCsvErrMsg   = "";       //重複(CSV)メッセージ
        $duplicateEntryErrMsg = "";       //重複(登録済)メッセージ
        $lastColErrMsg        = "";       //最終列文字エラーメッセージ
        $dateFormatErrMsg     = "";       //日付(フォーマット)エラーメッセージ

        $hissuSep          = "";
        $existenceSep      = "";
        $numericSep        = "";
        $lengthSep         = "";
        $valuesSep         = "";
        $duplicateCsvSep   = "";
        $duplicateEntrySep = "";
        $lastColSep        = "";
        $dateFormatSep     = "";

        //重複(CSV)チェック
        $dupFlg1 = $this->duplicateCsvCheck($duplicateCsv1, $primaryKey1, $data);
        if ($dupFlg1[0] == 1) {
            $returnFlg = $this->errSet($duplicateCsvErrFlg, $duplicateCsvErrMsg, "※重複(CSV):", $duplicateCsvSep, "重複1(". $dupFlg1[1] ."行目)");
        }

        $dupFlg2 = $this->duplicateCsvCheck($duplicateCsv2, $primaryKey2, $data);
        if ($dupFlg2[0] == 1) {
            $returnFlg = $this->errSet($duplicateCsvErrFlg, $duplicateCsvErrMsg, "※重複(CSV):", $duplicateCsvSep, "重複2(". $dupFlg2[1] ."行目)");
        }

        $dupFlg3 = $this->duplicateCsvCheck($duplicateCsv3, $primaryKey3, $data);
        if ($dupFlg3[0] == 1) {
            $returnFlg = $this->errSet($duplicateCsvErrFlg, $duplicateCsvErrMsg, "※重複(CSV):", $duplicateCsvSep, "重複3(". $dupFlg3[1] ."行目)");
        }

        $dupFlg4 = $this->duplicateCsvCheck($duplicateCsv4, $primaryKey4, $data);
        if ($dupFlg4[0] == 1) {
            $returnFlg = $this->errSet($duplicateCsvErrFlg, $duplicateCsvErrMsg, "※重複(CSV):", $duplicateCsvSep, "重複4(". $dupFlg4[1] ."行目)");
        }

        //重複(登録済)チェック
        if (knja040mQuery::schregnoExist($this, $data["SCHREGNO"], $db)) {
            $returnFlg = $this->errSet($duplicateEntryErrFlg, $duplicateEntryErrMsg, "※重複(登録済):", $duplicateEntrySep, "重複2");
        }

        $query = knja040mQuery::checkSchregEntGrdHistDat($data["SCHREGNO"], $data["SCHOOL_KIND"]);
        $rowCount = $db->getOne($query);
        if ($rowCount > "0") {
            $returnFlg = $this->errSet($duplicateEntryErrFlg, $duplicateEntryErrMsg, "※重複(登録済):", $duplicateEntrySep, "重複3");
        }
        foreach ($data_each as $key => $val) {
            //必須項目チェック
            $hissuArray = array("ENTERYEAR", "SCHREGNO", "ENT_DIV", "INOUTCD", "NAME", $this->lastColumn);
            if ($this->Properties["FRESHMAN_DAT_HissuCheck"] === '1') {
                $hissuArray[] = "CURRICULUM_YEAR";
                $hissuArray[] = "AREACD";
            }
            if (in_array($key, $hissuArray)) {
                if (!strlen(trim($data[$key]))) {
                    $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[1]);
                }
            }

            //年度が処理年度ではない場合
            if ($key == "ENTERYEAR" && $this->field["EXE_YEAR"] != $data[$key]) {
                $returnFlg = $this->errSet($valuesErrFlg, $valuesErrMsg, "※値不正:", $valuesSep, "年度不正".$val[1]);
            }
            //LASTCOLUMNの記載がない場合
            if ($key == $this->lastColumn && strlen(trim($data[$key])) != 0 && $data[$key] !== $this->lastColumn) {
                $returnFlg = $this->errSet($lastColErrFlg, $lastColErrMsg, "※最終列文字不正:", $lastColSep, $this->lastColumn."でなければならない");
            }

            //存在チェック -- 入学区分
            if ($key == "ENT_DIV" && strlen(trim($data[$key])) != 0) {
                if (0 == $db->getOne(knja040mQuery::checkNameMst($this, "A002", $data[$key]))) {
                    $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[1]);
                }
            }
            //存在チェック -- 性別
            if ($key == "SEX" && strlen(trim($data[$key])) != 0) {
                if (0 == $db->getOne(knja040mQuery::checkNameMst($this, "Z002", $data[$key]))) {
                    $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[1]);
                }
            }

            //存在チェック -- 課程
            if ($key == "COURSECD" && strlen(trim($data[$key])) != 0) {
                if (0 == $db->getOne(knja040mQuery::checkCourseCd($this, $data[$key]))) {
                    $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[1]);
                }
            }
            //存在チェック -- 学科
            if ($key == "MAJORCD" && strlen(trim($data[$key])) != 0) {
                if (0 == $db->getOne(knja040mQuery::checkMajorCd($this, $data["COURSECD"], $data[$key]))) {
                    $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[1]);
                }
            }
            //存在チェック -- コースコード
            if ($key == "COURSECODE" && strlen(trim($data[$key])) != 0) {
                if (0 == $db->getOne(knja040mQuery::checkCourseCode($this, $data[$key]))) {
                    $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[1]);
                }
            }

            //生年月日と出身学校卒業日の日付型チェック
            if (in_array($key, array("BIRTHDAY", "FINSCHOOLGRADDATE"))) {
                if (common::isDate($data[$key]) == false) {
                    $returnFlg = $this->errSet($dateFormatErrFlg, $dateFormatErrMsg, "※日付(フォーマット):", $dateFormatSep, $val[1]);
                }
            }

            //数値チェック
            if ($this->Properties["useMusyouKaisu"] == "1") {
                if (in_array($key, array("ENTERYEAR", "INOUTCD", "COURSECD", "MAJORCD", "COURSECODE", "AREACD", "MUSYOU_KAISU"))) {
                    if ($data[$key] != "" && !is_numeric($data[$key])) {
                        $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, $val[1]);
                    }
                }
            } else {
                if (in_array($key, array("ENTERYEAR", "INOUTCD", "COURSECD", "MAJORCD", "COURSECODE", "AREACD"))) {
                    if ($data[$key] != "" && !is_numeric($data[$key])) {
                        $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, $val[1]);
                    }
                }
            }

            //文字数チェック
            if (strlen($data[$key]) > $val[0]) {
                $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[1]);
            }
        }

        if (!$returnFlg) {
            $checkError = $duplicateCsvErrMsg.$duplicateEntryErrMsg.$hissuErrMsg.$existenceErrMsg.$numericErrMsg.$lengthErrMsg.$valuesErrMsg.$lastColErrMsg.$dateFormatErrMsg;
            $checkError = $this->checkErrLength($checkError);
            knja040mQuery::insertQueryErr($db, $line, $checkError);
        }
        return $returnFlg;
    }

    public function &getExecModel()
    {
        //ファイルの文字列の取得
        $this->data_arr = array();
        $this->data_arr = $this->getFileString2();

        if ($this->warning) {
            return false;
        }

        //データの整合性チェック
        if ($this->isDataMatched($this->data_arr)) {
            $errFlg = $this->checkCsvErrQuery(PROGRAMID);
            if ($errFlg) {
                knja040mQuery::insertQueryCsv($this, $this->data_arr);
                $this->setMessage("MSG202", "更新件数".get_count($this->data_arr)."件");
            }
        }
    }

    //取込
    public function getFileString2()
    {
        $temp_arr = array();
        if (!is_uploaded_file($this->file["tmp_name"])) {        //HTTP POSTによりアップロードされたファイルかどうかを調べる
            $this->warning = "ファイル名が不正です。";
        } else {
            $checkdata = array("SCHREGNO"                   => array(8,     "学籍番号"),
                               "RELATIONSHIP"               => array(2,     "続柄"),
                               "GUARD_NAME"                 => array(60,    "保護者氏名"),
                               "GUARD_KANA"                 => array(120,   "保護者氏名かな"),
                               "GUARD_SEX"                  => array(1,     "性別"),
                               "GUARD_BIRTHDAY"             => array(10,    "生年月日"),
                               "GUARD_ZIPCD"                => array(8,     "郵便番号"),
                               "GUARD_ADDR1"                => array(150,    "住所1"),
                               "GUARD_ADDR2"                => array(150,    "住所2"),
                               "GUARD_TELNO"                => array(14,    "電話番号"),
                               "GUARD_FAXNO"                => array(14,    "FAX番号"),
                               "GUARD_E_MAIL"               => array(50,    "E-MAIL"),
                               "GUARD_JOBCD"                => array(2,     "職種コード"),
                               "GUARD_WORK_NAME"            => array(60,    "勤務先名称"),
                               "GUARD_WORK_TELNO"           => array(14,    "勤務先電話番号"),
                               "GUARANTOR_RELATIONSHIP"     => array(2,     "保証人続柄"),
                               "GUARANTOR_NAME"             => array(60,    "保証人氏名"),
                               "GUARANTOR_KANA"             => array(120,   "保証人氏名かな"),
                               "GUARANTOR_SEX"              => array(1,     "保証人性別"),
                               "GUARANTOR_ZIPCD"            => array(8,     "保証人郵便番号"),
                               "GUARANTOR_ADDR1"            => array(150,    "保証人住所1"),
                               "GUARANTOR_ADDR2"            => array(150,    "保証人住所2"),
                               "GUARANTOR_TELNO"            => array(14,    "保証人電話番号"),
                               "GUARANTOR_JOBCD"            => array(2,     "保証人職種コード"),
                               "PUBLIC_OFFICE"              => array(30,    "兼ねている公職"),
                               $this->lastColumn            => array(10,    $this->lastColumn));

            $db = Query::dbCheckOut();
            $db->autoCommit(false);

            //エラーデータの削除
            $query = $this->deleteQueryErr(PROGRAMID);
            $result = $db->query($query);

            $fp = fopen($this->file["tmp_name"], "r");
            $i  = 0;
            $line = 1;  //行数
            $tmp_schregno = array();
            $header = false;

            //取得主キー
            $primaryKey1 = array();
            $primaryKey2 = array();
            $duplicateCsv1 = array();
            $duplicateCsv2 = array();

            $primaryKey1 = $this->getPrimaryKey('GUARDIAN_DAT');
            $primaryKey2 = $this->getPrimaryKey('GUARDIAN_ADDRESS_DAT');

            while (!feof($fp)) {
                $buffer = fgets($fp, 4096);
                if (trim($buffer) != '') {
                    if (!$header && $this->field["HEADERCHECK"] == 1) {
                        $header = true;
                        $line++;
                        continue;
                    }
                    //文字コードを判別、改行コードを削除
                    $buffer = trim(i18n_convert($buffer, i18n_http_output(), "SJIS-win"));
                    $tmp    = common::csv2array($buffer);

                    $data = array("SCHREGNO"                  => (strlen(trim($tmp[4]))) ? sprintf("%08d", $tmp[4]) : "",
                                  "RELATIONSHIP"              => (strlen(trim($tmp[5]))) ? sprintf("%02d", $tmp[5]) : "",
                                  "GUARD_NAME"                => $tmp[6],
                                  "GUARD_KANA"                => $tmp[7],
                                  "GUARD_SEX"                 => $tmp[8],
                                  "GUARD_BIRTHDAY"            => $tmp[9],
                                  "GUARD_ZIPCD"               => $tmp[10],
                                  "GUARD_ADDR1"               => $tmp[11],
                                  "GUARD_ADDR2"               => $tmp[12],
                                  "GUARD_TELNO"               => $tmp[13],
                                  "GUARD_FAXNO"               => $tmp[14],
                                  "GUARD_E_MAIL"              => $tmp[15],
                                  "GUARD_JOBCD"               => (strlen(trim($tmp[16]))) ? sprintf("%02d", $tmp[16]) : "",
                                  "GUARD_WORK_NAME"           => $tmp[17],
                                  "GUARD_WORK_TELNO"          => $tmp[18],
                                  "GUARANTOR_RELATIONSHIP"    => (strlen(trim($tmp[19]))) ? sprintf("%02d", $tmp[19]) : "",
                                  "GUARANTOR_NAME"            => $tmp[20],
                                  "GUARANTOR_KANA"            => $tmp[21],
                                  "GUARANTOR_SEX"             => $tmp[22],
                                  "GUARANTOR_ZIPCD"           => $tmp[23],
                                  "GUARANTOR_ADDR1"           => $tmp[24],
                                  "GUARANTOR_ADDR2"           => $tmp[25],
                                  "GUARANTOR_TELNO"           => $tmp[26],
                                  "GUARANTOR_JOBCD"           => (strlen(trim($tmp[27]))) ? sprintf("%02d", $tmp[27]) : "",
                                  "PUBLIC_OFFICE"             => $tmp[28],
                                  "ISSUEDATE"                 => $this->field["GUARD_ISSUEDATE"],
                                  $this->lastColumn           => $tmp[29]);

                    //エラーチェック
                    if ($this->errCheck2($db, $data, $checkdata, $line, $tmp_schregno, $duplicateCsv1, $duplicateCsv2, $primaryKey1, $primaryKey2)) {
                        $temp_arr[$i] = $data;
                        $i++;
                        $tmp_schregno[] = $data["SCHREGNO"];
                    }
                    foreach ($primaryKey1 as $key => $val) {
                        $duplicateCsv1[$line][$val] = $data[$val];
                    }
                    foreach ($primaryKey2 as $key => $val) {
                        $duplicateCsv2[$line][$val] = $data[$val];
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
    public function errCheck2($db, $data, $data_each, $line, $tmp_schregno, $duplicateCsv1, $duplicateCsv2, $primaryKey1, $primaryKey2)
    {
        $returnFlg          = true;
        $hissuErrFlg        = false;    //必須エラーフラグ
        $existenceErrFlg    = false;    //存在エラーフラグ
        $numericErrFlg      = false;    //数字エラーフラグ
        $lengthErrFlg       = false;    //レングスエラーフラグ
        $duplicateCsvErrFlg = false;    //重複(CSV)エラーフラグ
        $lastColErrFlg      = false;    //最終列文字エラーフラグ

        $hissuErrMsg        = "";       //必須エラーメッセージ
        $existenceErrMsg    = "";       //存在エラーメッセージ
        $numericErrMsg      = "";       //数字エラーメッセージ
        $lengthErrMsg       = "";       //レングスエラーメッセージ
        $duplicateCsvErrMsg = "";       //重複(CSV)メッセージ
        $lastColErrMsg      = "";       //最終列文字エラーメッセージ

        $hissuSep        = "";
        $existenceSep    = "";
        $numericSep      = "";
        $lengthSep       = "";
        $duplicateCsvSep = "";
        $lastColSep      = "";
        $sonzaiarray     = array("RELATIONSHIP"         => "H201",
                             "GUARD_SEX"                => "Z002",
                             "GUARD_JOBCD"              => "H202",
                             "GUARANTOR_RELATIONSHIP"   => "H201",
                             "GUARANTOR_SEX"            => "Z002",
                             "GUARANTOR_JOBCD"          => "H202");
        //重複(CSV)チェック
        $dupFlg1 = $this->duplicateCsvCheck($duplicateCsv1, $primaryKey1, $data);
        if ($dupFlg1[0] == 1) {
            $returnFlg = $this->errSet($duplicateCsvErrFlg, $duplicateCsvErrMsg, "※重複(CSV):", $duplicateCsvSep, "重複1(". $dupFlg1[1] ."行目)");
        }

        $dupFlg2 = $this->duplicateCsvCheck($duplicateCsv2, $primaryKey2, $data);
        if ($dupFlg2[0] == 1) {
            $returnFlg = $this->errSet($duplicateCsvErrFlg, $duplicateCsvErrMsg, "※重複(CSV):", $duplicateCsvSep, "重複2(". $dupFlg2[1] ."行目)");
        }

        foreach ($data_each as $key => $val) {
            //必須項目チェック
            if (in_array($key, array("SCHREGNO","RELATIONSHIP",$this->lastColumn))) {
                if (!strlen(trim($data[$key]))) {
                    $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[1]);
                }
            }

            //LASTCOLUMNの記載がない場合
            if ($key == $this->lastColumn && strlen(trim($data[$key])) != 0 && $data[$key] !== $this->lastColumn) {
                $returnFlg = $this->errSet($lastColErrFlg, $lastColErrMsg, "※最終列文字不正:", $lastColSep, $this->lastColumn."でなければならない");
            }

            if (in_array($key, array("RELATIONSHIP","GUARD_SEX","GUARD_JOBCD","GUARANTOR_RELATIONSHIP","GUARANTOR_SEX","GUARANTOR_JOBCD"))) {
                if ($data[$key] != "" && 1 > $db->getOne(knja040mQuery::checkMstData($sonzaiarray[$key], $data[$key]))) {
                    $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[1]);
                }
            }

            if (in_array($key, array("GUARD_BIRTHDAY"))) {
                if ($data[$key] != "" && !$this->dateRep($data[$key])) {
                    $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, $val[1]);
                }
            }

            //数値チェック
            if (in_array($key, array("SCHREGNO","RELATIONSHIP","GUARD_JOBCD","GUARANTOR_JOBCD"))) {
                if ($data[$key] != "" && !is_numeric($data[$key])) {
                    $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, $val[1]);
                }
            }

            //文字数チェック
            if (strlen($data[$key]) > $val[0]) {
                $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[1]);
            }
        }

        if (!$returnFlg) {
            $checkError = $duplicateCsvErrMsg.$hissuErrMsg.$existenceErrMsg.$numericErrMsg.$lengthErrMsg.$lastColErrMsg;
            $checkError = $this->checkErrLength($checkError);
            knja040mQuery::insertQueryErr($db, $line, $checkError);
        }
        return $returnFlg;
    }

    /**
     * 日付変換 項目ﾁｪｯｸ後に行う。
     */
    public function dateRep($data)
    {
        if ($data!="") {
            $data = common::DateConv1(str_replace("-", "/", $data), 4);
            if ($data) {
                $data = str_replace("/", "-", $data);
            }
        }

        return $data;
    }

    //テンプレートの書き出し
    public function outputTmpFile()
    {
        $header_data = false;   //出力フラグ
        if ($this->field["OUTPUT"] == "1") {
            //ヘッダ
            $header_data = true;
            if ($this->field["DATADIV"] == 1) {
                //DB接続
                $db = Query::dbCheckOut();
                //CSVフォーマットType
                //     1：学校コード、卒業年月日
                // 1以外：学校コード、学校名、卒業年月日
                $query = knja040mQuery::getCsvFormatType();
                $formatType = $db->getOne($query);

                //DB切断
                Query::dbCheckIn($db);

                if ($this->field["HEADERCHECK"] == "1") {
                    $contents = implode($this->getFreshHead($formatType), ",")."\n";
                }
                $contents .= implode($this->getFreshTemlate($formatType), ",")."\n";
            } else {
                $contents  = ($this->field["HEADERCHECK"] == 1) ? implode($this->getHeadGuardian(), ",")."\n" : "";
                $contents .= implode($this->getReiGuardian(), ",")."\n";
            }
        } elseif ($this->field["OUTPUT"] == "3") {
            //エラー
            $headerDuplicate = array("ENTERYEAR"             => "入学処理年度",
                                    "SCHREGNO"               => "学籍番号",
                                    "BASE_SEQ"               => "学校SEQ",
                                    "ISSUEDATE"              => "住所開始日",
                                    "SCHOOL_KIND"            => "学校校種");

            $tables = array();
            if ($this->field["DATADIV"] == 2) {
                $tables = array("1" => "GUARDIAN_DAT", "2" => "GUARDIAN_ADDRESS_DAT");
            } else {
                $tables = array("1" => "FRESHMAN_DAT", "2" => "SCHREG_BASE_MST", "3" => "SCHREG_ENT_GRD_HIST_DAT", "4" => "SCHREG_BASE_DETAIL_MST");
            }
            $dupTable = $this->getDuplicateErrorHeader(PROGRAMID, $tables);
            if (sizeof($dupTable) > 0) {
                $headerErr = $this->duplicateCsvHeader($dupTable, $headerDuplicate);
                $contents .= implode($headerErr, ",")."\n";
            }
            $contents .= implode($this->getHeadErorr(), ",")."\n";
        }

        //ヘッダ以外
        if ($this->field["OUTPUT"] == "3") {
            //DB接続
            $db = Query::dbCheckOut();

            $query = $this->selectCsvErrQuery(PROGRAMID);

            //SQL文発行
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                if ($this->field["OUTPUT"]=="4") {                //データ
                    $row = str_replace(",", ".", $row);   //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
                }
                $contents .= implode($row, ",") ."\n";
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
                $couse = "新入生取り込み";
            }
            $filename = sprintf("%s.csv", $couse);
            common::downloadContents($contents, $filename, "SJIS-win");
            return true;
        } else {
            $this->warning = "該当データは、ありません。";
            return false;
        }
    }

    //データヘッダ
    public function getFreshHead($formatType)
    {
        $retFreshHead = array();
        $retFreshHead[] = "※入学年度";
        $retFreshHead[] = "※学籍番号";
        $retFreshHead[] = "※入学区分";
        $retFreshHead[] = "※学年";
        $retFreshHead[] = "組";
        $retFreshHead[] = "出席番号";
        $retFreshHead[] = "※内外区分";
        $retFreshHead[] = "※課程コード";
        $retFreshHead[] = "※学科コード";
        $retFreshHead[] = "コースコード";
        $retFreshHead[] = "※氏名";
        $retFreshHead[] = "氏名かな";
        $retFreshHead[] = "生年月日";
        $retFreshHead[] = "性別";
        if ($formatType == "1") {
            $retFreshHead[] = "出身学校コード";
            $retFreshHead[] = "出身学校卒業日";
        } else {
            $retFreshHead[] = "出身学校コード";
            $retFreshHead[] = "出身学校名";
            $retFreshHead[] = "出身学校卒業日";
        }
        $retFreshHead[] = "郵便番号";
        $retFreshHead[] = "住所１";
        $retFreshHead[] = "住所２";
        $retFreshHead[] = "電話番号";
        $retFreshHead[] = "FAX番号";
        $retFreshHead[] = "EMAIL";
        $retFreshHead[] = "緊急連絡先";
        $retFreshHead[] = "緊急連絡電話番号";
        $retFreshHead[] = "スカラシップ";
        if ($this->Properties["FRESHMAN_DAT_HissuCheck"] === '1') {
            $retFreshHead[] = "※課程入学年度";
            $retFreshHead[] = "※地区コード";
        } else {
            $retFreshHead[] = "課程入学年度";
            $retFreshHead[] = "地区コード";
        }
        $retFreshHead[] = "受験番号";
        if ($this->Properties["useMusyouKaisu"] == "1") {
            $retFreshHead[] = "無償回数";
        }
        $retFreshHead[] = "入試区分";
        $retFreshHead[] = $this->lastColumn;

        return $retFreshHead;
    }

    //データヘッダテンプレート
    public function getFreshTemlate($formatType)
    {
        $retFreshTemplate = array();
        $retFreshTemplate[] = "2003";
        $retFreshTemplate[] = "99999999";
        $retFreshTemplate[] = "1";
        $retFreshTemplate[] = "01";
        $retFreshTemplate[] = "001";
        $retFreshTemplate[] = "001";
        $retFreshTemplate[] = "1";
        $retFreshTemplate[] = "1";
        $retFreshTemplate[] = "001";
        $retFreshTemplate[] = "0001";
        $retFreshTemplate[] = "山田太郎";
        $retFreshTemplate[] = "やまだたろう";
        $retFreshTemplate[] = "1980-10-20";
        $retFreshTemplate[] = "1";
        if ($formatType == "1") {
            $retFreshTemplate[] = ($this->Properties["useFinschoolcdFieldSize"] == '12' ? "999999999999" : "9999999");
            $retFreshTemplate[] = "2002-03-31";
        } else {
            $retFreshTemplate[] = ($this->Properties["useFinschoolcdFieldSize"] == '12' ? "999999999999" : "9999999");
            $retFreshTemplate[] = "出身学校名";
            $retFreshTemplate[] = "2002-03-31";
        }
        $retFreshTemplate[] = "801-2211";
        $retFreshTemplate[] = "東京都○○市○○町";
        $retFreshTemplate[] = "一丁目２－３";
        $retFreshTemplate[] = "04-8765-4321";
        $retFreshTemplate[] = "03-1234-5678";
        $retFreshTemplate[] = "yamada@yamada.co.jp";
        $retFreshTemplate[] = "株式会社○○";
        $retFreshTemplate[] = "03-1234-5678";
        $retFreshTemplate[] = "99";
        $retFreshTemplate[] = "2003";
        $retFreshTemplate[] = "99";
        $retFreshTemplate[] = "99999";
        if ($this->Properties["useMusyouKaisu"] == "1") {
            $retFreshTemplate[] = "9";
        }
        $retFreshTemplate[] = "9";
        $retFreshTemplate[] = $this->lastColumn;

        return $retFreshTemplate;
    }

    //エラーヘッダ
    public function getHeadErorr()
    {
        $header_err = array("MSGROW"            => "行番号",
                            "MSGREMARK"         => "メッセージ");
        return $header_err;
    }

    //保護者ヘッダ
    public function getHeadGuardian()
    {
        $header = array("GRADE"                     => "学年",
                        "HR_CLASS"                  => "クラス",
                        "ATTENDNO"                  => "出席番号",
                        "NAME"                      => "氏名",
                        "SCHREGNO"                  => "※学籍番号",
                        "RELATIONSHIP"              => "※続柄",
                        "GUARD_NAME"                => "保護者氏名",
                        "GUARD_KANA"                => "保護者氏名かな",
                        "GUARD_SEX"                 => "性別",
                        "GUARD_BIRTHDAY"            => "生年月日",
                        "GUARD_ZIPCD"               => "郵便番号",
                        "GUARD_ADDR1"               => "住所1",
                        "GUARD_ADDR2"               => "住所2",
                        "GUARD_TELNO"               => "電話番号",
                        "GUARD_FAXNO"               => "FAX番号",
                        "GUARD_E_MAIL"              => "E-MAIL",
                        "GUARD_JOBCD"               => "職種コード",
                        "GUARD_WORK_NAME"           => "勤務先名称",
                        "GUARD_WORK_TELNO"          => "勤務先電話番号",
                        "GUARANTOR_RELATIONSHIP"    => "保証人続柄",
                        "GUARANTOR_NAME"            => "保証人氏名",
                        "GUARANTOR_KANA"            => "保証人氏名かな",
                        "GUARANTOR_SEX"             => "保証人性別",
                        "GUARANTOR_ZIPCD"           => "保証人郵便番号",
                        "GUARANTOR_ADDR1"           => "保証人住所1",
                        "GUARANTOR_ADDR2"           => "保証人住所2",
                        "GUARANTOR_TELNO"           => "保証人電話番号",
                        "GUARANTOR_JOBCD"           => "保証人職種コード",
                        "PUBLIC_OFFICE"             => "兼ねている公職",
                        $this->lastColumn           => $this->lastColumn);
        return $header;
    }

    //保護者ヘッダ例
    public function getReiGuardian()
    {
        $header_rei = array("GRADE"                     => "99",
                            "HR_CLASS"                  => "999",
                            "ATTENDNO"                  => "999",
                            "NAME"                      => "アルプ太郎",
                            "SCHREGNO"                  => "12345678",
                            "RELATIONSHIP"              => "99",
                            "GUARD_NAME"                => "全角20",
                            "GUARD_KANA"                => "全角40",
                            "GUARD_SEX"                 => "9",
                            "GUARD_BIRTHDAY"            => "9999-99-99",
                            "GUARD_ZIPCD"               => "999-9999",
                            "GUARD_ADDR1"               => "全角50",
                            "GUARD_ADDR2"               => "全角50",
                            "GUARD_TELNO"               => "9999-9999-9999",
                            "GUARD_FAXNO"               => "9999-9999-9999",
                            "GUARD_E_MAIL"              => "aaa123@bbb.co.jp",
                            "GUARD_JOBCD"               => "99",
                            "GUARD_WORK_NAME"           => "全角20",
                            "GUARD_WORK_TELNO"          => "9999-9999-9999",
                            "GUARANTOR_RELATIONSHIP"    => "99",
                            "GUARANTOR_NAME"            => "全角20",
                            "GUARANTOR_KANA"            => "全角40",
                            "GUARANTOR_SEX"             => "9",
                            "GUARANTOR_ZIPCD"           => "999-9999",
                            "GUARANTOR_ADDR1"           => "全角50",
                            "GUARANTOR_ADDR2"           => "全角50",
                            "GUARANTOR_TELNO"           => "9999-9999-9999",
                            "GUARANTOR_JOBCD"           => "99",
                            "PUBLIC_OFFICE"             => "全角10",
                            $this->lastColumn           => $this->lastColumn);
        return $header_rei;
    }
}
?>
