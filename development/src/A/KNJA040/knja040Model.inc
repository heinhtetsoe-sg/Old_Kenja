<?php

require_once('for_php7.php');

class knja040Model extends Model
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
    public $nameSLen;
    public $addrSLen;
    public $lastColumn;//CSVの最後の列の値

    public function knja040Model()
    {
        $this->new_year = CTRL_YEAR + 1;
        common::GetControlMaster_Fnc($this->controls);
    }

    public function init()
    {
        $this->getPropertiesAll();   //プロパティファイル
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
        $this->cmd              = VARS::request("cmd");
        $temp                   = VARS::post("COURSEMAJOR");
        $this->course           = substr($temp, 0, 1);
        $this->major            = substr($temp, 2, 3);
        $this->max_semester     = $this->controls["学期数"];             //学期数
        $this->max_semestername = knja040Query::getMaxSemester($this);   //MAX学期名

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
        $this->schoolKind = VARS::post("SCHOOL_KIND");
        $this->grade = VARS::post("GRADE");
        $this->nameSLen = $this->Properties["NAME_INPUT_SIZE"] != "" ? $this->Properties["NAME_INPUT_SIZE"] : 40;
        $this->addrSLen = $this->Properties["ADDR_INPUT_SIZE"] != "" ? $this->Properties["ADDR_INPUT_SIZE"] : 50;

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
                    $query = knja040Query::checkSchool();
                    $schoolDiv = $db->getRow($query, DB_FETCHMODE_ASSOC);
                    Query::dbCheckIn($db);
                    if ($schoolDiv["NAMESPARE2"] == "1") {
                        knja040Query::insertQueryHousei($this);
                    } else {
                        knja040Query::insertQuery($this);
                        knja040Query::insertClassFormationDat($this);             //新入生のデータを追加処理
                    }
                    $this->setMessage("MSG202", "更新件数".get_count($this->data_arr)."件");
                }
            }
        }
    }

    public function getFileString()
    {
        $temp_arr = array();
        $examLen = $this->Properties["examnoLen"] ? $this->Properties["examnoLen"] : "5";
        if (!is_uploaded_file($this->file["tmp_name"])) {        //HTTP POSTによりアップロードされたファイルかどうかを調べる
            $this->warning = "ファイル名が不正です。";
        } else {
            $checkdata["ENTERYEAR"]          = array(4,"入学処理年度");
            $checkdata["SCHREGNO"]           = array(8,"学籍番号");
            $checkdata["ENT_DIV"]            = array(1,"入学区分");
            $checkdata["HR_CLASS"]           = array(3,"組");
            $checkdata["ATTENDNO"]           = array(3,"出席番号");
            $checkdata["INOUTCD"]            = array(1,"内外区分");
            $checkdata["COURSECD"]           = array(1,"課程コード");
            $checkdata["MAJORCD"]            = array(3,"学科コード");
            $checkdata["COURSECODE"]         = array(4,"コースコード");
            $checkdata["NAME"]               = array(intval($this->nameSLen) * 3,"生徒氏名");
            $checkdata["NAME_KANA"]          = array(240,"生徒氏名かな");
            $checkdata["BIRTHDAY"]           = array(10,"生年月日");
            $checkdata["SEX"]                = array(1,"性別");
            if ($this->schoolKind == "P" || $this->schoolKind == "K") {
                $checkdata["FINSCHOOLNAME"]      = array(306,"出身学校名");
            } else {
                $checkdata["FINSCHOOLCD"]        = array(($this->Properties["useFinschoolcdFieldSize"] == '12' ? 12 : 7),"中学校コード");
                $checkdata["FINSCHOOLGRADDATE"]  = array(10,"中学卒業年月日");
            }
            $checkdata["ZIPCD"]              = array(8,"郵便番号");
            $checkdata["ADDRESS1"]           = array(intval($this->addrSLen) * 3,"住所1");
            $checkdata["ADDRESS2"]           = array(intval($this->addrSLen) * 3,"住所2");
            $checkdata["TELNO"]              = array(14,"電話番号");
            $checkdata["FAXNO"]              = array(14,"FAX番号");
            $checkdata["EMAIL"]              = array(50,"E-MAIL");
            $checkdata["EMERGENCYCALL"]      = array(60,"急用連絡先");
            $checkdata["EMERGENCYTELNO"]     = array(14,"急用電話番号");
            $checkdata["SCALASHIPDIV"]       = array(2,"スカラシップ");
            $checkdata["CURRICULUM_YEAR"]    = array(4,"課程入学年度");
            $checkdata["AREACD"]             = array(2,"地区コード");
            $checkdata["EXAMNO"]             = array($examLen,"受験番号");
            if ($this->Properties["UseMusyouKaisu"] == "1") {
                $checkdata["MUSYOU_KAISU"]       = array(1,"無償回数");
            }
            $checkdata[$this->lastColumn]    = array(10,$this->lastColumn);

            //エラーデータの削除
            $db = Query::dbCheckOut();
            $query = $this->deleteQueryErr(PROGRAMID);
            $db->query($query);

            //学校取得
            $query = knja040Query::checkSchool();
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
            $primaryKey3 = array();
            $duplicateCsv1 = array();
            $duplicateCsv2 = array();
            $duplicateCsv3 = array();
            $primaryKey1 = $this->getPrimaryKey('FRESHMAN_DAT');
            $primaryKey2 = $this->getPrimaryKey('CLASS_FORMATION_DAT');
            if ($this->schoolKind == "P" || $this->schoolKind == "K") {
                $primaryKey3 = $this->getPrimaryKey('SCHREG_BASE_DETAIL_MST');
            }

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
                    $dataCnt = 0;
                    $data["ENTERYEAR"]            = $tmp[$dataCnt++];
                    $data["SCHREGNO"]             = (strlen(trim($tmp[$dataCnt]))) ? sprintf("%08d", $tmp[$dataCnt]) : "";
                    $dataCnt++;
                    $data["ENT_DIV"]              = $tmp[$dataCnt++];
                    $data["HR_CLASS"]             = (strlen(trim($tmp[$dataCnt]))) ? sprintf("%03s", $tmp[$dataCnt]) : "";
                    $dataCnt++;
                    $data["ATTENDNO"]             = (strlen(trim($tmp[$dataCnt]))) ? sprintf("%03d", $tmp[$dataCnt]) : "";
                    $dataCnt++;
                    $data["INOUTCD"]              = $tmp[$dataCnt++];
                    $data["COURSECD"]             = $tmp[$dataCnt++];
                    $data["MAJORCD"]              = (strlen(trim($tmp[$dataCnt]))) ? sprintf("%03d", $tmp[$dataCnt]) : "";
                    $dataCnt++;
                    $data["COURSECODE"]           = (strlen(trim($tmp[$dataCnt]))) ? sprintf("%04d", $tmp[$dataCnt]) : "";
                    $dataCnt++;
                    $data["NAME"]                 = $tmp[$dataCnt++];
                    $data["NAME_KANA"]            = $tmp[$dataCnt++];
                    $data["BIRTHDAY"]             = $tmp[$dataCnt++];
                    $data["SEX"]                  = $tmp[$dataCnt++];
                    if ($this->schoolKind == "P" || $this->schoolKind == "K") {
                        $data["FINSCHOOLNAME"]        = $tmp[$dataCnt++];
                        $data["BASE_SEQ"]         = '002';
                    } else {
                        $data["FINSCHOOLCD"]          = (strlen(trim($tmp[$dataCnt]))) ? sprintf("%0".($this->Properties["useFinschoolcdFieldSize"] == '12' ? "12" : "7")."s", $tmp[$dataCnt]) : "";
                        $dataCnt++;
                        $data["FINSCHOOLGRADDATE"]    = $tmp[$dataCnt++];
                    }
                    $data["ZIPCD"]                = $tmp[$dataCnt++];
                    $data["ADDRESS1"]             = $tmp[$dataCnt++];
                    $data["ADDRESS2"]             = $tmp[$dataCnt++];
                    $data["TELNO"]                = $tmp[$dataCnt++];
                    $data["FAXNO"]                = $tmp[$dataCnt++];
                    $data["EMAIL"]                = $tmp[$dataCnt++];
                    $data["EMERGENCYCALL"]        = $tmp[$dataCnt++];
                    $data["EMERGENCYTELNO"]       = $tmp[$dataCnt++];
                    $data["SCALASHIPDIV"]         = (strlen(trim($tmp[$dataCnt]))) ? sprintf("%02d", $tmp[$dataCnt]) : "";
                    $dataCnt++;
                    $data["CURRICULUM_YEAR"]      = $tmp[$dataCnt++];
                    $data["AREACD"]               = $tmp[$dataCnt++];
                    $data["EXAMNO"]               = $tmp[$dataCnt++];
                    $data["SEMESTER"]             = 1;
                    if ($this->Properties["useMusyouKaisu"] == "1") {
                        $data["MUSYOU_KAISU"]         = $tmp[$dataCnt++];
                    }
                    $data[$this->lastColumn]       = $tmp[$dataCnt++];

                    //エラーチェック
                    if ($this->errCheck($db, $data, $checkdata, $line, $tmp_schregno, $schoolDiv["NAME1"], $duplicateCsv1, $duplicateCsv2, $duplicateCsv3, $primaryKey1, $primaryKey2, $primaryKey3)) {
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

                    if ($this->schoolKind == "P" || $this->schoolKind == "K") {
                        foreach ($primaryKey3 as $key => $val) {
                            $duplicateCsv3[$line][$val] = $data[$val];
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

    public function errCheck($db, $data, $data_each, $line, $tmp_schregno, $schoolDiv, $duplicateCsv1, $duplicateCsv2, $duplicateCsv3, $primaryKey1, $primaryKey2, $primaryKey3)
    {

        $returnFlg = true;
        $hissuErrFlg        = false;    //必須エラーフラグ
        $existenceErrFlg    = false;    //存在エラーフラグ
        $numericErrFlg      = false;    //数字エラーフラグ
        $lengthErrFlg       = false;    //レングスエラーフラグ
        $valuesErrFlg       = false;    //値エラーフラグ
        $orderErrFlg        = false;    //固有エラーフラグ
        $lastColErrFlg      = false;    //最終列文字エラーフラグ
        $dateFormatErrFlg   = false;    //日付(フォーマット)エラーフラグ
        $duplicateCsvErrFlg = false;    //重複エラーフラグ

        $hissuErrMsg        = "";       //必須エラーメッセージ
        $existenceErrMsg    = "";       //存在エラーメッセージ
        $numericErrMsg      = "";       //数字エラーメッセージ
        $lengthErrMsg       = "";       //レングスエラーメッセージ
        $valuesErrMsg       = "";       //値エラーメッセージ
        $orderErrMsg        = "";       //固有エラーメッセージ
        $lastColErrMsg      = "";       //最終列文字エラーメッセージ
        $dateFormatErrMsg   = "";       //日付(フォーマット)エラーメッセージ
        $duplicateCsvErrMsg = "";       //重複エラーメッセージ

        $hissuSep        = "";
        $existenceSep    = "";
        $numericSep      = "";
        $lengthSep       = "";
        $valuesSep       = "";
        $orderSep        = "";
        $lastColSep      = "";
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

        $dupFlg3 = $this->duplicateCsvCheck($duplicateCsv3, $primaryKey3, $data);
        if ($dupFlg3[0] == 1) {
            $returnFlg = $this->errSet($duplicateCsvErrFlg, $duplicateCsvErrMsg, "※重複(CSV):", $duplicateCsvSep, "重複3(". $dupFlg3[1] ."行目)");
        }

        foreach ($data_each as $key => $val) {
            //必須チェックプロパティ確認
            if ($this->Properties["FRESHMAN_DAT_HissuCheck"] === '1') {
                //必須項目チェック
                if (in_array($key, array("ENTERYEAR", "SCHREGNO", "ENT_DIV", "INOUTCD", "COURSECD", "MAJORCD", "NAME", "CURRICULUM_YEAR", "AREACD", $this->lastColumn))) {
                    if (!strlen(trim($data[$key]))) {
                        $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[1]);
                    }
                }
            } else {
                //必須項目チェック
                if (in_array($key, array("ENTERYEAR", "SCHREGNO", "ENT_DIV", "INOUTCD", "COURSECD", "MAJORCD", "NAME", $this->lastColumn))) {
                    if (!strlen(trim($data[$key]))) {
                        $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[1]);
                    }
                }
            }

            //年度が現在年度+1ではない場合
            if ($key == "ENTERYEAR" && $this->new_year != $data[$key]) {
                $returnFlg = $this->errSet($valuesErrFlg, $valuesErrMsg, "※値不正:", $valuesSep, "年度不正".$val[1]);
            }
            //LASTCOLUMNの記載がない場合
            if ($key == $this->lastColumn && strlen(trim($data[$key])) != 0 && $data[$key] !== $this->lastColumn) {
                $returnFlg = $this->errSet($lastColErrFlg, $lastColErrMsg, "※最終列文字不正:", $lastColSep, $this->lastColumn."でなければならない");
            }

            if ($this->field["SHORI_MEI"] == "1") {
                //学籍番号が処理年度の在籍データに存在している場合
                if ($key == "SCHREGNO" && knja040Query::schregnoExist($this, $data[$key], $db)) {
                    $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[1]);
                }
                //入学区分
                if ($key == "ENT_DIV" && strlen(trim($data[$key])) != 0) {
                    if (0 == knja040Query::getNameMst($db, $this->new_year, "A002", $data[$key])) {
                        $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[1]);
                    }
                }
                //性別
                if ($key == "SEX" && strlen(trim($data[$key])) != 0) {
                    if (0 == knja040Query::getNameMst($db, $this->new_year, "Z002", $data[$key])) {
                        $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[1]);
                    }
                }

                if (!in_array($schoolDiv, array("KINDAI", "KINJUNIOR"))) {
                    //コースがマスタにない場合
                    if ($key == "COURSECODE" && 0 == $db->getOne(knja040Query::getCourseCode($this, $data[$key]))) {
                        $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[1]);
                    }

                    //課程コードが指定されたものと異なる場合
                    if ($key == "COURSECD" && strcmp($this->course, $data[$key]) != 0) {
                        $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[1]);
                    }

                    //学科コードが指定されたものと異なる場合
                    if ($key == "MAJORCD" && strcmp($this->major, $data[$key]) != 0) {
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

                if ($this->Properties["chkNameInputHankaku"] == "1") {
                    if ($key == "NAME") {
                        //氏名で半角文字列が混ざっているか、チェック
                        if (!$this->chkHankaku($data[$key])) {
                            $returnFlg = $this->errSet($orderErrFlg, $orderErrMsg, "※固有エラー:", $orderSep, "半角不可".$val[1]);
                        }
                    }
                }
                if ($this->Properties["chkAddrInputHankaku"] == "1") {
                    if ($key == "ADDRESS1" || $key == "ADDRESS2") {
                        //住所で半角文字列が混ざっているか、チェック
                        if (!$this->chkHankaku($data[$key])) {
                            $returnFlg = $this->errSet($orderErrFlg, $orderErrMsg, "※固有エラー:", $orderSep, "半角不可".$val[1]);
                        }
                    }
                }

                //文字数チェック
                if ($this->Properties["NAME_INPUT_SIZE"] != "" && $key == "NAME") {
                    if (mb_strlen($data[$key], 'UTF-8') > $this->nameSLen) {
                        $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, "全角{$this->nameSLen}文字超過".$val[1]);
                    }
                } elseif ($this->Properties["ADDR_INPUT_SIZE"] != "" && in_array($key, array("ADDRESS1", "ADDRESS2"))) {
                    if (mb_strlen($data[$key], 'UTF-8') > $this->addrSLen) {
                        $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, "全角{$this->addrSLen}文字超過".$val[1]);
                    }
                } else {
                    if (strlen($data[$key]) > $val[0]) {
                        $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[1]);
                    }
                }
            } else {
                //削除処理は、ない。
            }
        }

        if (!$returnFlg) {
            $checkError = $duplicateCsvErrMsg.$hissuErrMsg.$existenceErrMsg.$numericErrMsg.$lengthErrMsg.$valuesErrMsg.$orderErrMsg.$lastColErrMsg.$dateFormatErrMsg;
            $checkError = $this->checkErrLength($checkError);
            knja040Query::insertQueryErr($db, $line, $checkError);
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
                    knja040Query::insertQueryCsv($this, $this->data_arr);
                } else {
                    knja040Query::deleteQueryCsv($this, $this->data_arr);
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
                               "GUARD_NAME"                => array(intval($this->nameSLen) * 3, "保護者氏名"),
                               "GUARD_KANA"                => array(120,"保護者氏名かな"),
                               "GUARD_SEX"                 => array(1,"性別"),
                               "GUARD_BIRTHDAY"            => array(10,"生年月日"),
                               "GUARD_ZIPCD"               => array(8,"郵便番号"),
                               "GUARD_ADDR1"               => array(intval($this->addrSLen) * 3, "住所1"),
                               "GUARD_ADDR2"               => array(intval($this->addrSLen) * 3, "住所2"),
                               "GUARD_TELNO"               => array(14,"電話番号"),
                               "GUARD_FAXNO"               => array(14,"FAX番号"),
                               "GUARD_E_MAIL"              => array(50,"E-MAIL"),
                               "GUARD_JOBCD"               => array(2,"職種コード"),
                               "GUARD_WORK_NAME"           => array(60,"勤務先名称"),
                               "GUARD_WORK_TELNO"          => array(14,"勤務先電話番号"),
                               "GUARANTOR_RELATIONSHIP"    => array(2,"保証人続柄"),
                               "GUARANTOR_NAME"            => array(intval($this->nameSLen) * 3, "保証人氏名"),
                               "GUARANTOR_KANA"            => array(120,"保証人氏名かな"),
                               "GUARANTOR_SEX"             => array(1,"保証人性別"),
                               "GUARANTOR_ZIPCD"           => array(8,"保証人郵便番号"),
                               "GUARANTOR_ADDR1"           => array(intval($this->addrSLen) * 3, "保証人住所1"),
                               "GUARANTOR_ADDR2"           => array(intval($this->addrSLen) * 3, "保証人住所2"),
                               "GUARANTOR_TELNO"           => array(14,"保証人電話番号"),
                               "GUARANTOR_JOBCD"           => array(2,"保証人職種コード"),
                               "PUBLIC_OFFICE"             => array(30,"兼ねている公職"),
                               $this->lastColumn           => array(10,$this->lastColumn));

            //エラーデータの削除
            $db = Query::dbCheckOut();
            $query = $this->deleteQueryErr(PROGRAMID);
            $db->query($query);
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
                                  $this->lastColumn           => $tmp[29],
                                  "ISSUEDATE"                 => $this->field["GUARD_ISSUEDATE"]);

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
        $returnFlg = true;
        $hissuErrFlg        = false;    //必須エラーフラグ
        $existenceErrFlg    = false;    //存在エラーフラグ
        $numericErrFlg      = false;    //数字エラーフラグ
        $lengthErrFlg       = false;    //レングスエラーフラグ
        $orderErrFlg        = false;    //固有エラーフラグ
        $lastColErrFlg      = false;    //最終列文字エラーフラグ
        $duplicateCsvErrFlg = false;    //重複エラーフラグ

        $hissuErrMsg        = "";       //必須エラーメッセージ
        $existenceErrMsg    = "";       //存在エラーメッセージ
        $numericErrMsg      = "";       //数字エラーメッセージ
        $lengthErrMsg       = "";       //レングスエラーメッセージ
        $orderErrMsg        = "";       //固有エラーメッセージ
        $lastColErrMsg      = "";       //最終列文字エラーメッセージ
        $duplicateCsvErrMsg = "";       //重複エラーメッセージ

        $hissuSep        = "";
        $existenceSep    = "";
        $numericSep      = "";
        $lengthSep       = "";
        $orderSep        = "";
        $lastColSep      = "";
        $duplicateCsvSep = "";
        $sonzaiarray = array("RELATIONSHIP"             => "H201",
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
            if (in_array($key, array("SCHREGNO","RELATIONSHIP",$this->lastColumn))) {
                if (!strlen(trim($data[$key]))) {
                    $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[1]);
                }
            }

            //LASTCOLUMNの記載がない場合
            if ($key == $this->lastColumn && strlen(trim($data[$key])) != 0 && $data[$key] !== $this->lastColumn) {
                $returnFlg = $this->errSet($lastColErrFlg, $lastColErrMsg, "※最終列文字不正:", $lastColSep, $this->lastColumn."でなければならない");
            }

            if ($this->field["SHORI_MEI"] == "1") {
                if (in_array($key, array("RELATIONSHIP","GUARD_SEX","GUARD_JOBCD","GUARANTOR_RELATIONSHIP","GUARANTOR_SEX","GUARANTOR_JOBCD"))) {
                    if ($data[$key] != "" && 1 > $db->getOne(knja040Query::checkMstData($sonzaiarray[$key], $data[$key]))) {
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

                if ($this->Properties["chkNameInputHankaku"] == "1") {
                    if ($key == "GUARD_NAME" || $key == "GUARANTOR_NAME") {
                        //氏名で半角文字列が混ざっているか、チェック
                        if (!$this->chkHankaku($data[$key])) {
                            $returnFlg = $this->errSet($orderErrFlg, $orderErrMsg, "※固有エラー:", $orderSep, "半角不可".$val[1]);
                        }
                    }
                }
                if ($this->Properties["chkAddrInputHankaku"] == "1") {
                    if ($key == "GUARD_ADDR1" || $key == "GUARD_ADDR2" || $key == "GUARANTOR_ADDR1" || $key == "GUARANTOR_ADDR2") {
                        //住所で半角文字列が混ざっているか、チェック
                        if (!$this->chkHankaku($data[$key])) {
                            $returnFlg = $this->errSet($orderErrFlg, $orderErrMsg, "※固有エラー:", $orderSep, "半角不可".$val[1]);
                        }
                    }
                }

                //文字数チェック
                if ($this->Properties["NAME_INPUT_SIZE"] != "" && ($key == "GUARD_NAME" || $key == "GUARANTOR_NAME")) {
                    if (mb_strlen($data[$key], 'UTF-8') > $this->nameSLen) {
                        $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, "全角{$this->nameSLen}文字超過".$val[1]);
                    }
                } elseif ($this->Properties["ADDR_INPUT_SIZE"] != "" && in_array($key, array("GUARD_ADDR1", "GUARD_ADDR2", "GUARANTOR_ADDR1", "GUARANTOR_ADDR2"))) {
                    if (mb_strlen($data[$key], 'UTF-8') > $this->addrSLen) {
                        $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, "全角{$this->addrSLen}文字超過".$val[1]);
                    }
                } else {
                    if (strlen($data[$key]) > $val[0]) {
                        $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[1]);
                    }
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
            $checkError = $duplicateCsvErrMsg.$hissuErrMsg.$existenceErrMsg.$numericErrMsg.$lengthErrMsg.$orderErrMsg.$lastColErrMsg;
            $checkError = $this->checkErrLength($checkError);
            knja040Query::insertQueryErr($db, $line, $checkError);
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
                if ($this->schoolKind == "P" || $this->schoolKind == "K") {
                    if ($this->Properties["useMusyouKaisu"] == "1") {
                        //必須チェックプロパティ確認
                        if ($this->Properties["FRESHMAN_DAT_HissuCheck"] === '1') {
                            $contents = ($this->headercheck == 1) ? "※入学年度,※学籍番号,※入学区分,組,出席番号,※内外区分,※課程コード,※学科コード,コースコード,※氏名,氏名かな,生年月日,性別,出身学校名,郵便番号,住所１,住所２,電話番号,FAX番号,EMAIL,緊急連絡先,緊急連絡電話番号,スカラシップ,※課程入学年度,※地区コード,受験番号,無償回数,".$this->lastColumn."\n" : "";
                            $contents.= "2003,99999999,1,1,001,1,1,001,0001,山田太郎,やまだたろう,1980-10-20,1,出身学校名,801-2211,東京都○○市○○町,一丁目２－３,04-8765-4321,03-1234-5678,yamada@yamada.co.jp,株式会社○○,03-1234-5678,99,2003,99,99999,9,".$this->lastColumn;
                        } else {
                            $contents = ($this->headercheck == 1) ? "※入学年度,※学籍番号,※入学区分,組,出席番号,※内外区分,※課程コード,※学科コード,コースコード,※氏名,氏名かな,生年月日,性別,出身学校名,郵便番号,住所１,住所２,電話番号,FAX番号,EMAIL,緊急連絡先,緊急連絡電話番号,スカラシップ,課程入学年度,地区コード,受験番号,無償回数,".$this->lastColumn."\n" : "";
                            $contents.= "2003,99999999,1,1,001,1,1,001,0001,山田太郎,やまだたろう,1980-10-20,1,出身学校名,801-2211,東京都○○市○○町,一丁目２－３,04-8765-4321,03-1234-5678,yamada@yamada.co.jp,株式会社○○,03-1234-5678,99,2003,99,99999,9,".$this->lastColumn;
                        }
                    } else {
                        //必須チェックプロパティ確認
                        if ($this->Properties["FRESHMAN_DAT_HissuCheck"] === '1') {
                            $contents = ($this->headercheck == 1) ? "※入学年度,※学籍番号,※入学区分,組,出席番号,※内外区分,※課程コード,※学科コード,コースコード,※氏名,氏名かな,生年月日,性別,出身学校名,郵便番号,住所１,住所２,電話番号,FAX番号,EMAIL,緊急連絡先,緊急連絡電話番号,スカラシップ,※課程入学年度,※地区コード,受験番号,".$this->lastColumn."\n" : "";
                            $contents.= "2003,99999999,1,1,001,1,1,001,0001,山田太郎,やまだたろう,1980-10-20,1,出身学校名,801-2211,東京都○○市○○町,一丁目２－３,04-8765-4321,03-1234-5678,yamada@yamada.co.jp,株式会社○○,03-1234-5678,99,2003,99,99999,".$this->lastColumn;
                        } else {
                            $contents = ($this->headercheck == 1) ? "※入学年度,※学籍番号,※入学区分,組,出席番号,※内外区分,※課程コード,※学科コード,コースコード,※氏名,氏名かな,生年月日,性別,出身学校名,郵便番号,住所１,住所２,電話番号,FAX番号,EMAIL,緊急連絡先,緊急連絡電話番号,スカラシップ,課程入学年度,地区コード,受験番号,".$this->lastColumn."\n" : "";
                            $contents.= "2003,99999999,1,1,001,1,1,001,0001,山田太郎,やまだたろう,1980-10-20,1,出身学校名,801-2211,東京都○○市○○町,一丁目２－３,04-8765-4321,03-1234-5678,yamada@yamada.co.jp,株式会社○○,03-1234-5678,99,2003,99,99999,".$this->lastColumn;
                        }
                    }
                } else {
                    if ($this->Properties["useMusyouKaisu"] == "1") {
                        //必須チェックプロパティ確認
                        if ($this->Properties["FRESHMAN_DAT_HissuCheck"] === '1') {
                            $contents = ($this->headercheck == 1) ? "※入学年度,※学籍番号,※入学区分,組,出席番号,※内外区分,※課程コード,※学科コード,コースコード,※氏名,氏名かな,生年月日,性別,出身学校コード,出身学校卒業日,郵便番号,住所１,住所２,電話番号,FAX番号,EMAIL,緊急連絡先,緊急連絡電話番号,スカラシップ,※課程入学年度,※地区コード,受験番号,無償回数,".$this->lastColumn."\n" : "";
                            $contents.= "2003,99999999,1,1,001,1,1,001,0001,山田太郎,やまだたろう,1980-10-20,1,".($this->Properties["useFinschoolcdFieldSize"] == '12' ? "999999999999" : "9999999").",2002-03-31,801-2211,東京都○○市○○町,一丁目２－３,04-8765-4321,03-1234-5678,yamada@yamada.co.jp,株式会社○○,03-1234-5678,99,2003,99,99999,9,".$this->lastColumn;
                        } else {
                            $contents = ($this->headercheck == 1) ? "※入学年度,※学籍番号,※入学区分,組,出席番号,※内外区分,※課程コード,※学科コード,コースコード,※氏名,氏名かな,生年月日,性別,出身学校コード,出身学校卒業日,郵便番号,住所１,住所２,電話番号,FAX番号,EMAIL,緊急連絡先,緊急連絡電話番号,スカラシップ,課程入学年度,地区コード,受験番号,無償回数,".$this->lastColumn."\n" : "";
                            $contents.= "2003,99999999,1,1,001,1,1,001,0001,山田太郎,やまだたろう,1980-10-20,1,".($this->Properties["useFinschoolcdFieldSize"] == '12' ? "999999999999" : "9999999").",2002-03-31,801-2211,東京都○○市○○町,一丁目２－３,04-8765-4321,03-1234-5678,yamada@yamada.co.jp,株式会社○○,03-1234-5678,99,2003,99,99999,9,".$this->lastColumn;
                        }
                    } else {
                        //必須チェックプロパティ確認
                        if ($this->Properties["FRESHMAN_DAT_HissuCheck"] === '1') {
                            $contents = ($this->headercheck == 1) ? "※入学年度,※学籍番号,※入学区分,組,出席番号,※内外区分,※課程コード,※学科コード,コースコード,※氏名,氏名かな,生年月日,性別,出身学校コード,出身学校卒業日,郵便番号,住所１,住所２,電話番号,FAX番号,EMAIL,緊急連絡先,緊急連絡電話番号,スカラシップ,※課程入学年度,※地区コード,受験番号,".$this->lastColumn."\n" : "";
                            $contents.= "2003,99999999,1,1,001,1,1,001,0001,山田太郎,やまだたろう,1980-10-20,1,".($this->Properties["useFinschoolcdFieldSize"] == '12' ? "999999999999" : "9999999").",2002-03-31,801-2211,東京都○○市○○町,一丁目２－３,04-8765-4321,03-1234-5678,yamada@yamada.co.jp,株式会社○○,03-1234-5678,99,2003,99,99999,".$this->lastColumn;
                        } else {
                            $contents = ($this->headercheck == 1) ? "※入学年度,※学籍番号,※入学区分,組,出席番号,※内外区分,※課程コード,※学科コード,コースコード,※氏名,氏名かな,生年月日,性別,出身学校コード,出身学校卒業日,郵便番号,住所１,住所２,電話番号,FAX番号,EMAIL,緊急連絡先,緊急連絡電話番号,スカラシップ,課程入学年度,地区コード,受験番号,".$this->lastColumn."\n" : "";
                            $contents.= "2003,99999999,1,1,001,1,1,001,0001,山田太郎,やまだたろう,1980-10-20,1,".($this->Properties["useFinschoolcdFieldSize"] == '12' ? "999999999999" : "9999999").",2002-03-31,801-2211,東京都○○市○○町,一丁目２－３,04-8765-4321,03-1234-5678,yamada@yamada.co.jp,株式会社○○,03-1234-5678,99,2003,99,99999,".$this->lastColumn;
                        }
                    }
                }
            } else {
                $contents  = ($this->headercheck == 1) ? implode($this->getHeadGuardian(), ",")."\n" : "";
                $contents .= implode($this->getReiGuardian(), ",")."\n";
            }
        } elseif ($this->field["OUTPUT"] == "3") {
            //エラー
            $tables = array();
            if ($this->field["DATADIV"] == 2) {
                $tables = array("1" => "GUARDIAN_DAT", "2" => "GUARDIAN_ADDRESS_DAT");
            } else {
                if ($this->schoolKind == "P" || $this->schoolKind == "K") {
                    $tables = array("1" => "FRESHMAN_DAT", "2" => "CLASS_FORMATION_DAT", "3" => "SCHREG_BASE_DETAIL_MST");
                } else {
                    $tables = array("1" => "FRESHMAN_DAT", "2" => "CLASS_FORMATION_DAT");
                }
            }
            $dupTable = $this->getDuplicateErrorHeader(PROGRAMID, $tables);
            $header = array();
            $header["ENTERYEAR"] = "入学年度";
            $header["YEAR"]      = "入学年度";
            $header["SCHREGNO"]  = "学籍番号";
            $header["SEMESTER"]  = "学期";
            $header["ISSUEDATE"] = "住所開始日";
            $header["BASE_SEQ"]  = "学籍基礎SEQ";
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

    public function chkHankaku($chkStr)
    {
        foreach (preg_split('//u', $chkStr, -1, PREG_SPLIT_NO_EMPTY) as $idx => $v) {
            if (strlen($v) == 1) {
                return false;
            }
        }
        return true;
    }
}
?>
