<?php

require_once('for_php7.php');

class knja040aModel extends Model
{
    public $cmd;
    public $new_year;    //対象入学年度
    public $course;
    public $major;
    public $file;
    public $data_arr = array();
    public $controls = array();
    public $max_semestername;
    public $maxsemester;
    public $field = array();
    public $grade;
    public $lastColumn;//CSVの最後の列の値

    public function knja040aModel()
    {
        $this->new_year = CTRL_YEAR + 1;
        common::GetControlMaster_Fnc($this->controls);
    }

    public function init()
    {
        $this->cmd              = VARS::request("cmd");
        $temp                   = VARS::post("COURSEMAJOR");
        $this->course           = substr($temp, 0, 1);
        $this->major            = substr($temp, 2, 3);
        $this->max_semester     = $this->controls["学期数"];             //学期数
        $this->max_semestername = knja040aQuery::getMaxSemester($this);   //MAX学期名

        $this->field = array("OUTPUT"           =>  VARS::post("OUTPUT"),            //出力取込種別
                             "SHORI_MEI"        =>  VARS::post("SHORI_MEI"),         //処理名
                             "GUARD_ADDR_FLG"   =>  VARS::post("GUARD_ADDR_FLG"),    //肩書き出力
                             "GUARD_ISSUEDATE"  =>  VARS::post("GUARD_ISSUEDATE"),   //住所開始日
                             "GUARD_EXPIREDATE" =>  VARS::post("GUARD_EXPIREDATE"),  //住所終了日
                             "DATADIV"          =>  VARS::post("DATADIV")            //対象データ
                             );

        if (VARS::get("PROGRAMID")) {
            $this->headercheck = "1";
        }
        $this->headercheck = VARS::post("HEADERCHECK");

        $this->file = VARS::file("FILE");
        $this->grade = VARS::post("GRADE");

        $this->getPropertiesAll();
        $this->finschoolcdKeta = ($this->Properties["useFinschoolcdFieldSize"] == '12' ? 12 : 7);

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
                    $db = Query::dbCheckOut();
                    $query = knja040aQuery::checkSchool();
                    $schoolDiv = $db->getRow($query, DB_FETCHMODE_ASSOC);
                    Query::dbCheckIn($db);
                    if ($schoolDiv["NAMESPARE2"] == "1") {
                        knja040aQuery::insertQueryHousei($this);
                    } else {
                        knja040aQuery::insertQuery($this);
                        knja040aQuery::insertClassFormationDat($this);             //新入生のデータを追加処理
                    }
                    $this->setMessage("MSG202", "更新件数".get_count($this->data_arr)."件");
                }
            }
        }
    }

    public function getFileString()
    {
        $temp_arr = array();
        if (!is_uploaded_file($this->file["tmp_name"])) {        //HTTP POSTによりアップロードされたファイルかどうかを調べる
            $this->warning = "ファイル名が不正です。";
        } else {
            $checkdata = array("ENTERYEAR"         => array(4,"入学処理年度"),
                               "SCHREGNO"          => array(8,"学籍番号"),
                               "ENT_DIV"           => array(1,"入学区分"),
                               "HR_CLASS"          => array(3,"組"),
                               "ATTENDNO"          => array(3,"出席番号"),
                               "INOUTCD"           => array(1,"内外区分"),
                               "COURSECD"          => array(1,"課程コード"),
                               "MAJORCD"           => array(3,"学科コード"),
                               "COURSECODE"        => array(4,"コースコード"),
                               "NAME"              => array(120,"生徒氏名"),
                               "NAME_KANA"         => array(240,"生徒氏名かな"),
                               "BIRTHDAY"          => array(10,"生年月日"),
                               "SEX"               => array(1,"性別"),
                               "FROM_SCHOOL_CD"    => array($this->finschoolcdKeta,"中学校コード"),
                               "FINSCHOOLGRADDATE" => array(10,"中学卒業年月日"),
                               "ZIPCD"             => array(8,"郵便番号"),
                               "ADDRESS1"          => array(75,"住所1"),
                               "ADDRESS2"          => array(75,"住所2"),
                               "TELNO"             => array(14,"電話番号"),
                               "FAXNO"             => array(14,"FAX番号"),
                               "EMAIL"             => array(20,"E-MAIL"),
                               "EMERGENCYCALL"     => array(60,"急用連絡先"),
                               "EMERGENCYTELNO"    => array(14,"急用電話番号"),
                               "SCALASHIPDIV"      => array(2,"スカラシップ"),
                               "CURRICULUM_YEAR"   => array(4,"課程入学年度"));

            $umejidata = array("SCHREGNO"        => 8,
                               "HR_CLASS"        => 3,
                               "ATTENDNO"        => 3,
                               "MAJORCD"         => 3,
                               "COURSECODE"      => 4,
                               "FROM_SCHOOL_CD"  => 7,
                               "SCALASHIPDIV"    => 2);

            //DB接続
            $db = Query::dbCheckOut();

            $csvTable = array();
            $query = knja040aQuery::getCsvTableData();
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $csvTable[] = array("FIELDNAME" => $row["NAME"], "VALUE" => "", "LENGTH" => $row["LENGTH"]);
            }
            $result->free();

            //CSVデータの削除
            $query = knja040aQuery::deleteQueryNyugakuList();
            $db->query($query);

            //西暦変換用
            $defyear = $db->getRow(knja040aQuery::getSeireki(), DB_FETCHMODE_ASSOC);

            //エラーデータの削除
            $query = $this->deleteQueryErr(PROGRAMID);
            $db->query($query);

            //学校取得
            $query = knja040aQuery::checkSchool();
            $schoolDiv = $db->getRow($query, DB_FETCHMODE_ASSOC);

            Query::dbCheckIn($db);

            $db = Query::dbCheckOut();
            $db->autoCommit(false);

            $fp = fopen($this->file["tmp_name"], "r");
            $i  = 0;
            $line = 1;  //行数
            $tmp_schregno = array();
            $header = false;
            $primaryKey1 = array();
            $primaryKey2 = array();
            $duplicateCsv1 = array();
            $duplicateCsv2 = array();
            $primaryKey1 = $this->getPrimaryKey('FRESHMAN_DAT');
            $primaryKey2 = $this->getPrimaryKey('CLASS_FORMATION_DAT');
            while (!feof($fp)) {
                $buffer = fgets($fp, 4096);
                if (trim($buffer) != '') {
                    if (!$header && $this->headercheck == 1) {
                        $header = true;
                        $line++;
                        continue;
                    }
                    //文字コードを判別、改行コードを削除
                    $buffer = trim(i18n_convert($buffer, i18n_http_output(), "SJIS-win"));
                    $tmp    = common::csv2array($buffer);
                    $data = array();
                    for ($tmpCnt = 0; $tmpCnt < get_count($tmp); $tmpCnt++) {
                        if ($umejidata[$csvTable[$tmpCnt]["FIELDNAME"]] != "") {
                            $tmp[$tmpCnt] = (strlen(trim($tmp[$tmpCnt]))) ? sprintf("%0".$umejidata[$csvTable[$tmpCnt]["FIELDNAME"]]."d", $tmp[$tmpCnt]) : "";
                        }
                        if ($tmp[$tmpCnt] == $this->lastColumn) { //LASTCOLUMNは最後なので取り込まない
                            break;
                        }
                        if ($csvTable[$tmpCnt]["FIELDNAME"] != "") {
                            // CSV項目がTEMP1の場合
                            if ($csvTable[$tmpCnt]["FIELDNAME"] == "TEMP1") {
                                if (strlen($tmp[$tmpCnt]) > $csvTable[$tmpCnt]["LENGTH"]) {
                                    // 項目のLENGTHに収まるように設定
                                    $tmp[$tmpCnt] = substr($tmp[$tmpCnt], 0, $csvTable[$tmpCnt]["LENGTH"]);
                                }
                            }
                            $data[$csvTable[$tmpCnt]["FIELDNAME"]] = $tmp[$tmpCnt];
                        }
                    }
                    //CSVデータ登録
                    $queryInsertNyugaku = knja040aQuery::insertNyugakuList($data);
                    //卒業日付
                    $data["FINSCHOOLGRADDATE"] = $data["ENTERYEAR"]."-03-31";
                    //生年月日
                    $birthday = $data["BIRTHDAY_YMD"]."-".sprintf("%02d", $data["TEMP102"])."-".sprintf("%02d", $data["TEMP103"]);
                    $data["BIRTHDAY"] = $birthday;
                    //氏名
                    $data["NAME"] = $data["STUDENT_SEI"]."　".$data["STUDENT_MEI"];
                    //氏名かな
                    $data["NAME_KANA"] = $data["STUDENT_SEIKANA"]."　".$data["STUDENT_MEIKANA"];
                    //性別
                    if ($data["SEX_CD"] == '2') { //女
                        $data["SEX"] = '2';
                    } elseif ($data["SEX_CD"] == '1') { //男
                        $data["SEX"] = '1';
                    } elseif (!strlen($data["SEX_CD"])) { //空
                        $data["SEX"] = null;
                    } else { //それ以外はエラーとなるコードをセット(1・2以外なら何でもよい)
                        $data["SEX"] = '9';
                    }
                    //出身学校
                    $data["FINSCHOOLCD"] = $data["FROM_SCHOOL_CD"];
                    //郵便番号
                    if ($data["STUDENT_YUBIN"]) {
                        $data["ZIPCD"] = substr($data["STUDENT_YUBIN"], 0, 3)."-".substr($data["STUDENT_YUBIN"], 3);
                    }
                    //住所1
                    $data["ADDR1"] = $data["STUDENT_JUSHO"];
                    //住所2
                    $data["ADDR2"] = $data["TEMP109"];
                    //電話
                    $data["TELNO"] = $data["STUDENT_TEL"];
                    $data["SEMESTER"]  = 1;
                    //エラーチェック
                    if ($this->errCheck($db, $data, $checkdata, $line, $tmp_schregno, $schoolDiv["NAME1"], $duplicateCsv1, $duplicateCsv2, $primaryKey1, $primaryKey2)) {
                        //CSVデータ登録
                        $db->query($queryInsertNyugaku);

                        $temp_arr[$i] = $data;
                        $i++;
                        $tmp_schregno[] = $data["SCHREGNO"];
                    }

                    foreach ($primaryKey1 as $key => $val) {
                        $duplicateCsv1[$line][$val] = $data[$val];
                    }

                    foreach ($primaryKey2 as $key => $val) {
                        if ($val == 'YEAR') {
                            $val = "ENTERYEAR";
                            $primaryKey2[$key] = $val;
                            $duplicateCsv2[$line][$val] = $data[$val];
                        } else {
                            $duplicateCsv2[$line][$val] = $data[$val];
                        }
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

    public function errCheck($db, $data, $data_each, $line, $tmp_schregno, $schoolDiv, $duplicateCsv1, $duplicateCsv2, $primaryKey1, $primaryKey2)
    {

        $returnFlg = true;
        $hissuErrFlg        = false;    //必須エラーフラグ
        $existenceErrFlg    = false;    //存在エラーフラグ
        $numericErrFlg      = false;    //数字エラーフラグ
        $lengthErrFlg       = false;    //レングスエラーフラグ
        $valuesErrFlg       = false;    //値エラーフラグ
        $dateFormatErrFlg   = false;    //日付(フォーマット)エラーフラグ
        $duplicateCsvErrFlg = false;    //重複エラーフラグ

        $hissuErrMsg        = "";       //必須エラーメッセージ
        $existenceErrMsg    = "";       //存在エラーメッセージ
        $numericErrMsg      = "";       //数字エラーメッセージ
        $lengthErrMsg       = "";       //レングスエラーメッセージ
        $valuesErrMsg       = "";       //値エラーメッセージ
        $dateFormatErrMsg   = "";       //日付(フォーマット)エラーメッセージ
        $duplicateCsvErrMsg = "";       //重複エラーメッセージ

        $hissuSep        = "";
        $existenceSep    = "";
        $numericSep      = "";
        $lengthSep       = "";
        $valuesSep       = "";
        $dateFormatSep   = "";
        $duplicateCsvSep = "";

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
            if (in_array($key, array("ENTERYEAR", "SCHREGNO", "INOUTCD", "COURSECD", "MAJORCD"))) {
                if (!strlen(trim($data[$key]))) {
                    $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[1]);
                }
            }

            //年度が現在年度+1ではない場合
            if ($key == "ENTERYEAR" && $this->new_year != $data[$key]) {
                $returnFlg = $this->errSet($valuesErrFlg, $valuesErrMsg, "※値不正:", $valuesSep, "年度不正".$val[1]);
            }

            if ($this->field["SHORI_MEI"] == "1") {
                //学籍番号が処理年度の在籍データに存在している場合
                if ($key == "SCHREGNO" && knja040aQuery::schregnoExist($this, $data[$key], $db)) {
                    $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[1]);
                }
                //入学区分
                if ($key == "ENT_DIV" && strlen(trim($data[$key])) != 0) {
                    if (0 == knja040aQuery::getNameMst($db, $this->new_year, "A002", $data[$key])) {
                        $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[1]);
                    }
                }
                //性別
                if ($key == "SEX" && strlen(trim($data[$key])) != 0) {
                    if (0 == knja040aQuery::getNameMst($db, $this->new_year, "Z002", $data[$key])) {
                        $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[1]);
                    }
                }

                if (!in_array($schoolDiv, array("KINDAI", "KINJUNIOR"))) {
                    //コースがマスタにない場合
                    if ($key == "COURSECODE" && 0 == $db->getOne(knja040aQuery::getCourseCode($this, $data[$key]))) {
                        $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[1]);
                    }

                    //課程コードが指定されたものと異なる場合
                    if ($key == "COURSECD" && 0 == $db->getOne(knja040aQuery::getCourseCd($this, $data[$key]))) {
                        $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[1]);
                    }

                    //学科コードが指定されたものと異なる場合
                    if ($key == "MAJORCD" && 0 == $db->getOne(knja040aQuery::getMajorCd($this, $data[$key]))) {
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
                if (in_array($key, array("ENTERYEAR", "INOUTCD", "COURSECD", "MAJORCD", "COURSECODE", "CURRICULUM_YEAR"))) {
                    if ($data[$key] != "" && !is_numeric($data[$key])) {
                        $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, $val[1]);
                    }
                }

                //文字数チェック
                if (strlen($data[$key]) > $val[0]) {
                    $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[1]);
                }
            } else {
                //削除処理は、ない。
            }
        }

        if (!$returnFlg) {
            $checkError = $duplicateCsvErrMsg.$hissuErrMsg.$existenceErrMsg.$numericErrMsg.$lengthErrMsg.$valuesErrMsg.$dateFormatErrMsg;
            $checkError = $this->checkErrLength($checkError);
            knja040aQuery::insertQueryErr($db, $line, $checkError);
        }
        return $returnFlg;
    }

    /**
     * 実行モデルを返します
    */
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
                if ($this->field["SHORI_MEI"] == "1") {
                    knja040aQuery::insertQueryCsv($this, $this->data_arr);
                } else {
                    knja040aQuery::deleteQueryCsv($this, $this->data_arr);
                }

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
            $checkdata = array("SCHREGNO"                  => array(8,"学籍番号"),
                               "RELATIONSHIP"              => array(2,"続柄"),
                               "GUARD_NAME"                => array(60,"保護者氏名"),
                               "GUARD_KANA"                => array(120,"保護者氏名かな"),
                               "GUARD_SEX"                 => array(1,"性別"),
                               "GUARD_BIRTHDAY"            => array(10,"生年月日"),
                               "GUARD_ZIPCD"               => array(8,"郵便番号"),
                               "GUARD_ADDR1"               => array(75,"住所1"),
                               "GUARD_ADDR2"               => array(75,"住所2"),
                               "GUARD_TELNO"               => array(14,"電話番号"),
                               "GUARD_FAXNO"               => array(14,"FAX番号"),
                               "GUARD_E_MAIL"              => array(20,"E-MAIL"),
                               "GUARD_JOBCD"               => array(2,"職種コード"),
                               "GUARD_WORK_NAME"           => array(60,"勤務先名称"),
                               "GUARD_WORK_TELNO"          => array(14,"勤務先電話番号"),
                               "GUARANTOR_RELATIONSHIP"    => array(2,"保証人続柄"),
                               "GUARANTOR_NAME"            => array(60,"保証人氏名"),
                               "GUARANTOR_KANA"            => array(120,"保証人氏名かな"),
                               "GUARANTOR_SEX"             => array(1,"保証人性別"),
                               "GUARANTOR_ZIPCD"           => array(8,"保証人郵便番号"),
                               "GUARANTOR_ADDR1"           => array(75,"保証人住所1"),
                               "GUARANTOR_ADDR2"           => array(75,"保証人住所2"),
                               "GUARANTOR_TELNO"           => array(14,"保証人電話番号"),
                               "GUARANTOR_JOBCD"           => array(2,"保証人職種コード"),
                               "PUBLIC_OFFICE"             => array(30,"兼ねている公職"));

            $umejidata = array("SCHREGNO"        => 8,
                               "RELATIONSHIP"    => 2,
                               "GUARD_JOBCD"     => 2,
                               "GUARANTOR_JOBCD" => 2);

            $db = Query::dbCheckOut();
            $db->autoCommit(false);

            $csvTable = array();
            $query = knja040aQuery::getCsvTableData();
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $csvTable[] = array("FIELDNAME" => $row["NAME"], "VALUE" => "");
            }
            $result->free();

            //CSVデータの削除
            $query = knja040aQuery::deleteQueryNyugakuList();
            $db->query($query);

            //エラーデータの削除
            $query = $this->deleteQueryErr(PROGRAMID);
            $result = $db->query($query);

            $fp = fopen($this->file["tmp_name"], "r");
            $i  = 0;
            $line = 1;  //行数
            $header = false;
            $primaryKey1 = array();
            $primaryKey2 = array();
            $duplicateCsv1 = array();
            $duplicateCsv2 = array();
            $primaryKey1 = $this->getPrimaryKey('GUARDIAN_DAT');
            $primaryKey2 = $this->getPrimaryKey('GUARDIAN_ADDRESS_DAT');

            while (!feof($fp)) {
                $buffer = fgets($fp, 4096);
                if (trim($buffer) != '') {
                    if (!$header && $this->headercheck == 1) {
                        $header = true;
                        $line++;
                        continue;
                    }
                    //文字コードを判別、改行コードを削除
                    $buffer = trim(i18n_convert($buffer, i18n_http_output(), "SJIS-win"));
                    $tmp    = common::csv2array($buffer);
                    $data = array();
                    for ($tmpCnt = 0; $tmpCnt < get_count($tmp); $tmpCnt++) {
                        if ($umejidata[$csvTable[$tmpCnt]["FIELDNAME"]] != "") {
                            $tmp[$tmpCnt] = (strlen(trim($tmp[$tmpCnt]))) ? sprintf("%0".$umejidata[$csvTable[$tmpCnt]["FIELDNAME"]]."d", $tmp[$tmpCnt]) : "";
                        }
                        if ($tmp[$tmpCnt] == $this->lastColumn) { //LASTCOLUMNは最後なので取り込まない
                            break;
                        }
                        if ($csvTable[$tmpCnt]["FIELDNAME"] != "") {
                            $data[$csvTable[$tmpCnt]["FIELDNAME"]] = $tmp[$tmpCnt];
                        }
                    }

                    //CSVデータ登録
                    $queryInsertNyugaku = knja040aQuery::insertNyugakuList($data);
                    //続柄
                    $data["RELATIONSHIP"] = "01";
                    //氏名
                    $data["GUARD_NAME"] = $data["HOGOSHA_NAME"];
                    //氏名かな
                    $data["GUARD_KANA"] = $data["HOGOSHA_KANA"];
                    //性別
                    $data["GUARD_SEX"] = "1";
                    //郵便番号
                    if ($data["HOGOSHA_YUBIN_NO"]) {
                        $data["GUARD_ZIPCD"] = substr($data["HOGOSHA_YUBIN_NO"], 0, 3)."-".substr($data["HOGOSHA_YUBIN_NO"], 3);
                    }
                    //住所1
                    $data["GUARD_ADDR1"] = $data["HOGOSHA_JUSHO"];
                    //住所2
                    $data["GUARD_ADDR2"] = $data["HOGOSHA_JUSHO2"];
                    //電話
                    $data["GUARD_TELNO"] = $data["HOGOSHA_TEL"];

                    //保証人氏名
                    $data["GUARANTOR_NAME"] = $data["HOGOSHA_NAME"];
                    //保証人氏名かな
                    $data["GUARANTOR_KANA"] = $data["HOGOSHA_KANA"];
                    //保証人性別
                    $data["GUARANTOR_SEX"] = "1";
                    //保証人郵便番号
                    if ($data["HOGOSHA_YUBIN_NO"]) {
                        $data["GUARANTOR_ZIPCD"] = substr($data["HOGOSHA_YUBIN_NO"], 0, 3)."-".substr($data["HOGOSHA_YUBIN_NO"], 3);
                    }
                    //保証人住所1
                    $data["GUARANTOR_ADDR1"] = $data["HOGOSHA_JUSHO"];
                    //保証人住所2
                    $data["GUARANTOR_ADDR2"] = $data["HOGOSHA_JUSHO2"];
                    //保証人電話
                    $data["GUARANTOR_TELNO"] = $data["HOGOSHA_TEL"];
                    $data["ISSUEDATE"]       = $this->field["GUARD_ISSUEDATE"];
                    //エラーチェック
                    if ($this->errCheck2($db, $data, $checkdata, $line, $duplicateCsv1, $duplicateCsv2, $primaryKey1, $primaryKey2)) {
                        //CSVデータ登録
                        $db->query($queryInsertNyugaku);

                        $temp_arr[$i] = $data;
                        $i++;
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
    public function errCheck2($db, $data, $data_each, $line, $duplicateCsv1, $duplicateCsv2, $primaryKey1, $primaryKey2)
    {
        $returnFlg = true;
        $hissuErrFlg        = false;    //必須エラーフラグ
        $existenceErrFlg    = false;    //存在エラーフラグ
        $numericErrFlg      = false;    //数字エラーフラグ
        $lengthErrFlg       = false;    //レングスエラーフラグ
        $duplicateCsvErrFlg = false;    //重複エラーフラグ

        $hissuErrMsg        = "";       //必須エラーメッセージ
        $existenceErrMsg    = "";       //存在エラーメッセージ
        $numericErrMsg      = "";       //数字エラーメッセージ
        $lengthErrMsg       = "";       //レングスエラーメッセージ
        $duplicateCsvErrMsg = "";       //重複エラーメッセージ

        $hissuSep        = "";
        $existenceSep    = "";
        $numericSep      = "";
        $lengthSep       = "";
        $duplicateCsvSep = "";
        $sonzaiarray     = array("RELATIONSHIP"         => "H201",
                             "GUARD_SEX"                => "Z002",
                             "GUARD_JOBCD"              => "H202",
                             "GUARANTOR_RELATIONSHIP"   => "H201",
                             "GUARANTOR_SEX"            => "Z002",
                             "GUARANTOR_JOBCD"          => "H202");
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
            if (in_array($key, array("SCHREGNO", "RELATIONSHIP"))) {
                if (!strlen(trim($data[$key]))) {
                    $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[1]);
                }
            }

            if ($this->field["SHORI_MEI"] == "1") {
                if (in_array($key, array("RELATIONSHIP","GUARD_SEX","GUARD_JOBCD","GUARANTOR_RELATIONSHIP","GUARANTOR_SEX","GUARANTOR_JOBCD"))) {
                    if ($data[$key] != "" && 1 > $db->getOne(knja040aQuery::checkMstData($sonzaiarray[$key], $data[$key]))) {
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
            } else {
                //数値チェック
                if (in_array($key, array("SCHREGNO"))) {
                    if ($data[$key] != "" && !is_numeric($data[$key])) {
                        $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, $val[1]);
                    }
                }
            }
        }

        if (!$returnFlg) {
            $checkError = $duplicateCsvErrMsg.$hissuErrMsg.$existenceErrMsg.$numericErrMsg.$lengthErrMsg;
            $checkError = $this->checkErrLength($checkError);
            knja040aQuery::insertQueryErr($db, $line, $checkError);
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
            $contents = ($this->headercheck == 1) ? "入学年度,学籍番号,入学区分,組,出席番号,内外区分,課程コード,学科コード,コースコード,受付校,４桁受付番号,受付番号*,姓＜通名＞*,名＜通名＞*,氏名＜通名＞,外字使用,ふりがな姓,ふりがな名,ふりがな,通名確認*,本名,性別,中学コード*,出身中学校名,その他中学校名,第１志望学科コード,第１志望科類・類型名,希望校コード,希望校府教委コード,第１志望校名,第１志望第２順位学科コード,第１志望第２順位学科名,第１志望第２順位希望校コード,第１志望第２順位希望校名,第１志望第３順位学科コード,第１志望第３順位学科名,第１志望第３順位希望校コード,第１志望第３順位希望校名,校名指定なし,第２志望科コード,第２志望科類・類型名,第２志望第２順位学科コード,第２志望第２順位科名,第２志望校コード,第２志望校名,専願,活,バス停番号,バス停名,第２志望通学圏,小学校区コード,小学校区,在卒,1R入力領域,1R国,1R社,1R数,1R理,1R音,1R美,1R体,1R技・家,1R英,1R5,1R4,1R4*2,1R合計,2R入力領域,2R国,2R社,2R数,2R理,2R音,2R美,2R体,2R技・家,2R英,2R5,2R4,2R4*2,2R合計,3R入力領域,3R国,3R社,3R数,3R理,3R音,3R美,3R体,3R技・家,3R英,3R5,3R4,3R4*2,3R合計,Ｒ合計,AT国,AT社,AT数,AT理,AT英,AT合計,総合計,願書提出先高校コード,過年度コード,性別コード*,合格学科コード,入学決定校コード,入学種別コード,生年,生月,生日,保護者氏名,郵便番号１,郵便番号２,新郵便番号,住所１,住所２,電話番号,判定,Ａ,Ｂ,甲類合格,入学校名,入学種別,備考,受付校コード（２桁）,入学予定年月日*,卒業見込み年月日,保護者ふりがな,保護者郵便番号,保護者現住所,保護者現住所２,保護者電話番号,課程学科コード*,課程入学年度,".$this->lastColumn."\n" : "";
            $contents.= "2003,99999999,1,1,1,1,1,1,1,高　教,1001,711001,生徒,600,生徒　６００,×,せいと,600,せいと　６００,×,,男,900,京都府中学校,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,1,,,,2001,3,14,保護者　６００,601,1,6010001,京都府０１,,075-414-5700,,,,,,,,,2008,,,6010001,京都府０１,,075-414-5700,101001,,".$this->lastColumn;
        } elseif ($this->field["OUTPUT"] == "3") {
            //エラー
            $tables = array();
            if ($this->field["DATADIV"] == 2) {
                $tables = array("1" => "GUARDIAN_DAT", "2" => "GUARDIAN_ADDRESS_DAT");
            } else {
                $tables = array("1" => "FRESHMAN_DAT", "2" => "CLASS_FORMATION_DAT");
            }
            $dupTable = $this->getDuplicateErrorHeader(PROGRAMID, $tables);
            $header = array();
            $header["ENTERYEAR"] = "入学年度";
            $header["YEAR"]      = "入学年度";
            $header["SCHREGNO"]  = "学籍番号";
            $header["SEMESTER"]  = "学期";
            $header["ISSUEDATE"] = "住所開始日";
            if (sizeof($dupTable) > 0) {
                $headerErr = $this->duplicateCsvHeader($dupTable, $header);
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

    //エラーヘッダ
    public function getHeadErorr()
    {
        $header_err = array("MSGROW"            => "行番号",
                            "MSGREMARK"         => "メッセージ");
        return $header_err;
    }
}
?>
