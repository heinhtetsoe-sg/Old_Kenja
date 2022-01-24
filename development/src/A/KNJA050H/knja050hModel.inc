<?php

require_once('for_php7.php');
class knja050hModel extends Model
{
    public $cmd;
    public $course;
    public $major;
    public $file;
    public $data_arr = array();
    public $controls = array();
    public $field = array();
    public $grade;
    public $lastColumn;//CSVの最後の列の値

    public function knja050hModel()
    {
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

        $this->field = array("YEAR"             =>  VARS::post("YEAR"),              //処理年度
                             "SEMESTER"         =>  VARS::post("SEMESTER"),          //処理学期
                             "OUTPUT"           =>  VARS::post("OUTPUT"),            //出力取込種別
                             "SHORI_MEI"        =>  VARS::post("SHORI_MEI"),         //処理名
                             "GUARD_ISSUEDATE"  =>  VARS::post("GUARD_ISSUEDATE"),   //住所開始日
                             "GUARD_EXPIREDATE" =>  VARS::post("GUARD_EXPIREDATE"),  //住所終了日
                             "DATADIV"          =>  VARS::post("DATADIV")            //対象データ
                             );

        if (VARS::get("PROGRAMID")) {
            $this->headercheck = "1";
        }
        $this->headercheck = VARS::post("HEADERCHECK");

        $this->file        = VARS::file("FILE");
        $this->schoolKind  = VARS::post("SCHOOL_KIND");
        $this->coursemajor = VARS::post("COURSEMAJOR");

        $this->lastColumn = 'DUMMY';
        if ($this->Properties["csv_LastColumn"] != '') {
            $this->lastColumn = $this->Properties["csv_LastColumn"];
        }
    }

    public function &getUpdateModel()
    {
        if ($this->field["DATADIV"] == 2) {
            // 保護者
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
                    if ($this->field["SHORI_MEI"] == "1") {
                        // 更新
                        knja050hQuery::insertQueryNyugaku($this);
                    } else {
                        // 削除
                        knja050hQuery::deleteQueryNyugaku($this);
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
            $db = Query::dbCheckOut();

            //チェック用 CSV取込項目
            $checkdata = array();
            $checkdata["ENTERYEAR"]          = array(4,    "HISSU",     "INT",   "入学処理年度");
            $checkdata["SEMESTER"]           = array(1,    "HISSU",     "INT",   "学期");
            $checkdata["SCHREGNO"]           = array(8,    "HISSU",     "INT2",  "学籍番号");
            $checkdata["ENT_DATE"]           = array(10,   "HISSU",     "DATE",  "入学日");
            $checkdata["ENT_DIV"]            = array(1,    "HISSU",     "INT",   "入学区分");
            $checkdata["GRADE"]              = array(2,    "HISSU",     "INT2",  "学年");
            $checkdata["HR_CLASS"]           = array(3,    "HISSU",     "INT2",  "組");
            $checkdata["ATTENDNO"]           = array(3,    "HISSU",     "INT2",  "出席番号");
            $checkdata["INOUTCD"]            = array(1,    "HISSU",     "INT",   "内外区分");
            $checkdata["COURSECD"]           = array(1,    "HISSU",     "INT",   "課程コード");
            $checkdata["MAJORCD"]            = array(3,    "HISSU",     "INT2",  "学科コード");
            $checkdata["COURSECODE"]         = array(4,    "",          "INT2",  "コースコード");
            $checkdata["NAME"]               = array(120,  "HISSU",     "",      "生徒氏名");
            $checkdata["NAME_KANA"]          = array(240,  "",          "",      "生徒氏名かな");
            $checkdata["BIRTHDAY"]           = array(10,   "",          "DATE",  "生年月日");
            $checkdata["SEX"]                = array(1,    "",          "INT",   "性別");
            $checkdata["FINSCHOOLCD"]        = array(7,    "",          "INT2",  "出身学校コード");
            $checkdata["FINSCHOOLGRADDATE"]  = array(10,   "",          "DATE",  "出身学校卒業日");
            $checkdata["PRISCHOOLCD"]        = array(7,    "",          "INT2",  "塾コード");
            $checkdata["DORMITORY_FLG"]      = array(1,    "FLG",       "INT",   "寮生フラグ");
            $checkdata["ZIPCD"]              = array(8,    "",          "",      "郵便番号");
            $checkdata["ADDRESS1"]           = array(90,   "",          "",      "住所1");
            $checkdata["ADDRESS2"]           = array(90,   "",          "",      "住所2");
            $checkdata["TELNO"]              = array(14,   "",          "",      "電話番号");
            $checkdata["FAXNO"]              = array(14,   "",          "",      "FAX番号");
            $checkdata["EMAIL"]              = array(20,   "",          "",      "E-MAIL");
            $checkdata["EMERGENCYCALL"]      = array(60,   "",          "",      "急用連絡先");
            $checkdata["EMERGENCYTELNO"]     = array(14,   "",          "",      "急用電話番号");
            $checkdata["SCALASHIPDIV"]       = array(2,    "",          "INT2",  "スカラシップ");
            $checkdata["KATEI_ENTEXAMYEAR"]  = array(4,    "",          "INT",   "課程入学年度");
            $checkdata["TIKUCD"]             = array(2,    "",          "INT2",  "地区コード");
            $checkdata["EXAMNO"]             = array(10,   "",          "",      "受験番号");
            $checkdata["TESTDIV"]            = array(1,    "",          "INT2",  "入試種別");
            $checkdata["EXAM_TYPE"]          = array(2,    "",          "INT2",  "入試方式");
            $checkdata[$this->lastColumn]    = array(10,   "HISSU",     "",      $this->lastColumn);

            //エラーデータの削除
            $query = $this->deleteQueryErr(PROGRAMID);
            $db->query($query);

            $db->autoCommit(false);

            $fp = fopen($this->file["tmp_name"], "r");
            $i  = 0;
            $line = 1;  //行数
            $header = false;
            $primaryKey1 = array();
            $primaryKey2 = array();
            $primaryKey3 = array();
            $primaryKey4 = array();
            $primaryKey5 = array();
            $primaryKey6 = array();
            $duplicateCsv1 = array();
            $duplicateCsv2 = array();
            $duplicateCsv3 = array();
            $duplicateCsv4 = array();
            $duplicateCsv5 = array();
            $duplicateCsv6 = array();
            $primaryKey1 = $this->getPrimaryKey('NYUGAKU_LIST_ANYTIME_DAT');
            $primaryKey2 = $this->getPrimaryKey('SCHREG_BASE_MST');
            $primaryKey3 = $this->getPrimaryKey('SCHREG_BASE_DETAIL_MST');
            $primaryKey4 = $this->getPrimaryKey('SCHREG_REGD_DAT');
            $primaryKey5 = $this->getPrimaryKey('SCHREG_ADDRESS_DAT');
            $primaryKey6 = $this->getPrimaryKey('SCHREG_DOMITORY_HIST_DAT');

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

                    //配列の要素数チェック
                    $checkcount = get_count($tmp);

                    $dataCnt = 0;
                    $data = array();
                    $data["ENTERYEAR"]            = $tmp[$dataCnt++];
                    $data["SEMESTER"]             = $tmp[$dataCnt++];
                    $data["SCHREGNO"]             = $tmp[$dataCnt++];
                    $data["ENT_DATE"]             = $tmp[$dataCnt++];
                    $data["ENT_DIV"]              = $tmp[$dataCnt++];
                    $data["GRADE"]                = $tmp[$dataCnt++];
                    $data["HR_CLASS"]             = $tmp[$dataCnt++];
                    $data["ATTENDNO"]             = $tmp[$dataCnt++];
                    $data["INOUTCD"]              = $tmp[$dataCnt++];
                    $data["COURSECD"]             = $tmp[$dataCnt++];
                    $data["MAJORCD"]              = $tmp[$dataCnt++];
                    $data["COURSECODE"]           = $tmp[$dataCnt++];
                    $data["NAME"]                 = $tmp[$dataCnt++];
                    $data["NAME_KANA"]            = $tmp[$dataCnt++];
                    $data["BIRTHDAY"]             = $tmp[$dataCnt++];
                    $data["SEX"]                  = $tmp[$dataCnt++];
                    $data["FINSCHOOLCD"]          = $tmp[$dataCnt++];
                    $data["FINSCHOOLGRADDATE"]    = $tmp[$dataCnt++];
                    $data["PRISCHOOLCD"]          = $tmp[$dataCnt++];
                    $data["DORMITORY_FLG"]        = $tmp[$dataCnt++];
                    $data["ZIPCD"]                = $tmp[$dataCnt++];
                    $data["ADDRESS1"]             = $tmp[$dataCnt++];
                    $data["ADDRESS2"]             = $tmp[$dataCnt++];
                    $data["TELNO"]                = $tmp[$dataCnt++];
                    $data["FAXNO"]                = $tmp[$dataCnt++];
                    $data["EMAIL"]                = $tmp[$dataCnt++];
                    $data["EMERGENCYCALL"]        = $tmp[$dataCnt++];
                    $data["EMERGENCYTELNO"]       = $tmp[$dataCnt++];
                    $data["SCALASHIPDIV"]         = $tmp[$dataCnt++];
                    $data["KATEI_ENTEXAMYEAR"]    = $tmp[$dataCnt++];
                    $data["TIKUCD"]               = $tmp[$dataCnt++];
                    $data["EXAMNO"]               = $tmp[$dataCnt++];
                    $data["TESTDIV"]              = $tmp[$dataCnt++];
                    $data["EXAM_TYPE"]            = $tmp[$dataCnt++];
                    $data[$this->lastColumn]      = $tmp[$dataCnt++];

                    $data1 = array();
                    $data1["BASE_SEQ"]             = "014";
                    $data1["ISSUEDATE"]            = $this->field["GUARD_ISSUEDATE"];
                    $data1 = array_merge($data, $data1);
                    //項目数によるファイルチェック
                    if ($checkcount != $dataCnt) {
                        $this->warning = "CSVファイルを確認して下さい。\\nフォーマットが一致していません。";
                        return;
                    }

                    //エラーチェック
                    if ($this->errCheck($db, $data, $checkdata, $line, $duplicateCsv1, $duplicateCsv2, $duplicateCsv3, $duplicateCsv4, $duplicateCsv5, $duplicateCsv6, $primaryKey1, $primaryKey2, $primaryKey3, $primaryKey4, $primaryKey5, $primaryKey6, $data1)) {
                        foreach ($checkdata as $key => $val) {
                            //ゼロ埋め
                            if ($val[2] == "INT2") {
                                $data[$key] = sprintf("%0{$val[0]}d", $data[$key]);
                            }
                        }
                        $temp_arr[$i] = $data;
                        $i++;
                    }
                    foreach ($primaryKey1 as $key => $val) {
                        $duplicateCsv1[$line][$val] = $data1[$val];
                    }
                    foreach ($primaryKey2 as $key => $val) {
                        $duplicateCsv2[$line][$val] = $data1[$val];
                    }
                    foreach ($primaryKey3 as $key => $val) {
                        $duplicateCsv3[$line][$val] = $data1[$val];
                    }
                    foreach ($primaryKey4 as $key => $val) {
                        if ($val == 'YEAR') {
                            $val = "ENTERYEAR";
                            $primaryKey4[$key] = $val;
                            $duplicateCsv4[$line][$val] = $data1[$val];
                        } else {
                            $duplicateCsv4[$line][$val] = $data1[$val];
                        }
                    }
                    foreach ($primaryKey5 as $key => $val) {
                        $duplicateCsv5[$line][$val] = $data1[$val];
                    }
                    foreach ($primaryKey6 as $key => $val) {
                        if ($val == 'DOMI_ENTDAY') {
                            $val = "ENT_DATE";
                            $primaryKey6[$key] = $val;
                            $duplicateCsv6[$line][$val] = $data1[$val];
                        } else {
                            $duplicateCsv6[$line][$val] = $data1[$val];
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

    /********************/
    /*  エラーチェック  */
    /********************/
    public function errCheck($db, $data, $data_each, $line, $duplicateCsv1, $duplicateCsv2, $duplicateCsv3, $duplicateCsv4, $duplicateCsv5, $duplicateCsv6, $primaryKey1, $primaryKey2, $primaryKey3, $primaryKey4, $primaryKey5, $primaryKey6, $data1)
    {
        $returnFlg          = true;
        $hissuErrFlg        = false;    //必須エラーフラグ
        $existenceErrFlg    = false;    //存在エラーフラグ
        $numericErrFlg      = false;    //数字エラーフラグ
        $lengthErrFlg       = false;    //レングスエラーフラグ
        $otherErrFlg        = false;    //その他エラーフラグ
        $valuesErrFlg       = false;
        $masterErrFlg       = false;    //職員マスタにないエラーフラグ
        $duplicateCsvErrFlg = false;    //重複(CSV)エラーフラグ
        $lastColErrFlg      = false;    //最終列文字エラーフラグ

        $hissuErrMsg        = "";       //必須エラーメッセージ
        $existenceErrMsg    = "";       //存在エラーメッセージ
        $numericErrMsg      = "";       //数字エラーメッセージ
        $lengthErrMsg       = "";       //レングスエラーメッセージ
        $otherErrMsg        = "";       //その他エラーメッセージ
        $valuesErrMsg       = "";
        $masterErrMsg       = "";       //マスタにないエラーメッセージ
        $duplicateCsvErrMsg = "";       //重複(CSV)メッセージ
        $lastColErrMsg      = "";       //最終列文字エラーメッセージ

        $hissuSep        = "";
        $existenceSep    = "";
        $numericSep      = "";
        $lengthSep       = "";
        $otherSep        = "";
        $valuesSep       = "";
        $masterSep       = "";
        $duplicateCsvSep = "";
        $lastColSep      = "";

        $checkNameMstCd = array("ENT_DIV"       => "A002",
                                "SEX"           => "Z002"
                                );

        $dupFlg1 = $this->duplicateCsvCheck($duplicateCsv1, $primaryKey1, $data1);
        if ($dupFlg1[0] == 1) {
            $returnFlg = $this->errSet($duplicateCsvErrFlg, $duplicateCsvErrMsg, "※重複(CSV):", $duplicateCsvSep, "重複1(". $dupFlg1[1] ."行目)");
        }

        $dupFlg2 = $this->duplicateCsvCheck($duplicateCsv2, $primaryKey2, $data1);
        if ($dupFlg2[0] == 1) {
            $returnFlg = $this->errSet($duplicateCsvErrFlg, $duplicateCsvErrMsg, "※重複(CSV):", $duplicateCsvSep, "重複2(". $dupFlg2[1] ."行目)");
        }

        $dupFlg3 = $this->duplicateCsvCheck($duplicateCsv3, $primaryKey3, $data1);
        if ($dupFlg3[0] == 1) {
            $returnFlg = $this->errSet($duplicateCsvErrFlg, $duplicateCsvErrMsg, "※重複(CSV):", $duplicateCsvSep, "重複3(". $dupFlg3[1] ."行目)");
        }
        $dupFlg4 = $this->duplicateCsvCheck($duplicateCsv4, $primaryKey4, $data1);
        if ($dupFlg4[0] == 1) {
            $returnFlg = $this->errSet($duplicateCsvErrFlg, $duplicateCsvErrMsg, "※重複(CSV):", $duplicateCsvSep, "重複4(". $dupFlg4[1] ."行目)");
        }

        $dupFlg5 = $this->duplicateCsvCheck($duplicateCsv5, $primaryKey5, $data1);
        if ($dupFlg5[0] == 1) {
            $returnFlg = $this->errSet($duplicateCsvErrFlg, $duplicateCsvErrMsg, "※重複(CSV):", $duplicateCsvSep, "重複5(". $dupFlg5[1] ."行目)");
        }

        $dupFlg6 = $this->duplicateCsvCheck($duplicateCsv6, $primaryKey6, $data1);
        if ($dupFlg6[0] == 1) {
            $returnFlg = $this->errSet($duplicateCsvErrFlg, $duplicateCsvErrMsg, "※重複(CSV):", $duplicateCsvSep, "重複6(". $dupFlg6[1] ."行目)");
        }

        $schregno = ($data["SCHREGNO"] != "") ? sprintf("%08d", $data["SCHREGNO"]) : "";
        if ($this->field["SHORI_MEI"] == "1") {
            //学籍番号が処理年度の在籍データに存在している場合
            if ($data["SCHREGNO"] != "" && knja050hQuery::schregnoExist($this, $schregno, $db)) {
                $returnFlg = $this->errSet($duplicateDbErrFlg, $duplicateDbErrMsg, "※重複(登録済):", $duplicateDbSep, "重複2");
            }
            if ($data["SCHREGNO"] != "" && knja050HQuery::getSchregBaseDetail($db, $schregno)) {
                $returnFlg = $this->errSet($duplicateDbErrFlg, $duplicateDbErrMsg, "※重複(登録済):", $duplicateDbSep, "重複3");
            }
            if ($data["SCHREGNO"] != "" && knja050HQuery::getSchregDat($db, $data, $schregno)) {
                $returnFlg = $this->errSet($duplicateDbErrFlg, $duplicateDbErrMsg, "※重複(登録済):", $duplicateDbSep, "重複4");
            }
            if ($data["SCHREGNO"] != "" && knja050HQuery::getSchregAddressDat($db, $this, $schregno)) {
                $returnFlg = $this->errSet($duplicateDbErrFlg, $duplicateDbErrMsg, "※重複(登録済):", $duplicateDbSep, "重複5");
            }
            if ($data["ENT_DATE"] != "" && common::isDate($data["ENT_DATE"]) == true) {
                if (knja050HQuery::getSchregDomitoryHistDat($db, $schregno, $data)) {
                    $returnFlg = $this->errSet($duplicateDbErrFlg, $duplicateDbErrMsg, "※重複(登録済):", $duplicateDbSep, "重複6");
                }
            }
        }

        foreach ($data_each as $key => $val) {
            // 更新処理
            if ($this->field["SHORI_MEI"] == "1") {
                //必須項目チェック
                if ($val[1] == "HISSU" && !strlen(trim($data[$key]))) {
                    $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[3]);
                }
                //チェック用にゼロ埋め
                if ($val[2] == "INT2" && $data[$key] != "" && is_numeric($data[$key])) {
                    $data[$key] = sprintf("%0{$val[0]}d", $data[$key]);
                }

                //数値チェック
                if (($val[2] == "INT" || $val[2] == "INT2") && $data[$key] != "" && !is_numeric($data[$key])) {
                    $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, $val[3]);
                }
                //年度が画面と異なる場合
                if ($key == "ENTERYEAR" && $this->field["YEAR"] != $data[$key]) {
                    $returnFlg = $this->errSet($otherErrFlg, $otherErrMsg, "※その他:", $otherSep, "年度不正".$val[3]);
                }
                //LASTCOLUMNの記載がない場合
                if ($key == $this->lastColumn && strlen(trim($data[$key])) != 0 && $data[$key] !== $this->lastColumn) {
                    $this->errSet($lastColErrFlg, $lastColErrMsg, "※最終列文字不正:", $lastColSep, $this->lastColumn."でなければならない");
                }
                //名称マスタ
                if ($checkNameMstCd[$key] && $data[$key] != "") {
                    if (0 == knja050hQuery::getNameMst($db, $this->field["YEAR"], $checkNameMstCd[$key], $data[$key])) {
                        $returnFlg = $this->errSet($masterErrFlg, $masterErrMsg, "※マスタチェック:", $masterSep, $val[3]);
                    }
                }

                //課程コードが指定されたものと異なる場合
                if ($key == "COURSECD" && strcmp($this->course, $data[$key]) != 0) {
                    $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[3]);
                }
                //学科コードが指定されたものと異なる場合
                if ($key == "MAJORCD" && strcmp($this->major, $data[$key]) != 0) {
                    $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[3]);
                }
                //コースがマスタにない場合
                if ($key == "COURSECODE" && 0 == $db->getOne(knja050hQuery::getCourseCode($this, $data[$key]))) {
                    $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[3]);
                }
                //入試種別がマスタにない場合
                if ($key == "TESTDIV" && 0 == $db->getOne(knja050hQuery::getTestDivCode($this, $data[$key]))) {
                    $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[3]);
                }
                //入試方式がマスタにない場合
                if ($key == "EXAM_TYPE" && 0 == $db->getOne(knja050hQuery::getExamTypeCode($this, $data[$key]))) {
                    $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[3]);
                }

                //日付型チェック
                if ($val[2] == "DATE") {
                    if (common::isDate($data[$key]) == false) {
                        $returnFlg = $this->errSet($otherErrFlg, $otherErrMsg, "※日付(フォーマット):", $otherSep, $val[3]);
                    }
                }

                //フラグチェック(値は1かNULL)
                if ($val[1] == "FLG" && !($data[$key] == "1" || !strlen(trim($data[$key])))) {
                    $returnFlg = $this->errSet($valuesErrFlg, $valuesErrMsg, "※値不正:", $valuesSep, $val[3]);
                }

                //文字数チェック
                if (strlen($data[$key]) > $val[0]) {
                    $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[3]);
                }
            //削除チェック
            } else {
                //必須項目チェック
                if (($key == "SCHREGNO" || $key == "ENT_DATE") && !strlen(trim($data[$key]))) {
                    $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[3]);
                }
                //チェック用にゼロ埋め
                if ($key == "SCHREGNO" && $data[$key] != "" && is_numeric($data[$key])) {
                    $data[$key] = sprintf("%0{$val[0]}d", $data[$key]);
                }
                //数値チェック
                if (($key == "SCHREGNO") && $data[$key] != "" && !is_numeric($data[$key])) {
                    $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, $val[3]);
                }
                //日付型チェック
                if ($key == "ENT_DATE") {
                    if (common::isDate($data[$key]) == false) {
                        $returnFlg = $this->errSet($otherErrFlg, $otherErrMsg, "※日付(フォーマット):", $otherSep, $val[3]);
                    }
                }
                //年度が画面と異なる場合
                if ($key == "ENTERYEAR" && $this->field["YEAR"] != $data[$key]) {
                    $returnFlg = $this->errSet($otherErrFlg, $otherErrMsg, "※その他:", $otherSep, "年度不正".$val[3]);
                }
                //LASTCOLUMNの記載がない場合
                if ($key == $this->lastColumn && strlen(trim($data[$key])) != 0 && $data[$key] !== $this->lastColumn) {
                    $this->errSet($lastColErrFlg, $lastColErrMsg, "※最終列文字不正:", $lastColSep, $this->lastColumn."でなければならない");
                }

                //学籍番号と入学日が一致している場合のみ削除可能
                if (($key == "SCHREGNO") && common::isDate($data["ENT_DATE"]) == true) {
                    if (!knja050hQuery::schregnoExistBase($this, $data["SCHREGNO"], $data["ENT_DATE"], $db)) {
                        $returnFlg = $this->errSet($masterErrFlg, $masterErrMsg, "※マスタチェック:", $masterSep, $val[3]);
                    }
                }
                if (($key == "SCHREGNO") && !knja050HQuery::getSchregBaseDetail($db, $schregno)) {
                    $returnFlg = $this->errSet($masterErrFlg, $masterErrMsg, "※マスタチェック:", $masterSep, "学籍基礎詳細データ");
                }
                if (($key == "SCHREGNO") && !knja050HQuery::getSchregDat($db, $data, $schregno)) {
                    $returnFlg = $this->errSet($masterErrFlg, $masterErrMsg, "※マスタチェック:", $masterSep, "学籍在籍データ");
                }
                if (($key == "SCHREGNO") && !knja050HQuery::getSchregAddressDat($db, $this, $schregno)) {
                    $returnFlg = $this->errSet($masterErrFlg, $masterErrMsg, "※マスタチェック:", $masterSep, "学籍住所データ");
                }
                if (($key == "SCHREGNO") && common::isDate($data["ENT_DATE"]) == true) {
                    if (!knja050HQuery::getSchregDomitoryHistDat($db, $schregno, $data)) {
                        $returnFlg = $this->errSet($masterErrFlg, $masterErrMsg, "※マスタチェック:", $masterSep, "学籍寮生履歴データ");
                    }
                }
            }
        }

        if (!$returnFlg) {
            $checkError = $duplicateCsvErrMsg.$duplicateDbErrMsg.$hissuErrMsg.$existenceErrMsg.$numericErrMsg.$lengthErrMsg.$otherErrMsg.$valuesErrMsg.$masterErrMsg.$lastColErrMsg;
            $checkError = $this->checkErrLength($checkError);
            knja050hQuery::insertQueryErr($db, $line, $checkError);
        }
        return $returnFlg;
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

    /**
     * 実行モデルを返します(保護者)
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
                    knja050hQuery::insertQueryGuardian($this);
                } else {
                    knja050hQuery::deleteQueryCsv($this, $this->data_arr);
                }
                $this->setMessage("MSG202", "更新件数".get_count($this->data_arr)."件");
            }
        }
    }

    //取込(保護者)
    public function getFileString2()
    {
        $temp_arr = array();
        if (!is_uploaded_file($this->file["tmp_name"])) {        //HTTP POSTによりアップロードされたファイルかどうかを調べる
            $this->warning = "ファイル名が不正です。";
        } else {
            //チェック用 CSV取込項目
            $checkdata = array();
            $checkdata["ENTERYEAR"]                 = array(4,      "HISSU",    "INT",  "入学処理年度");
            $checkdata["SCHREGNO"]                  = array(8,      "HISSU",    "INT2", "学籍番号");
            $checkdata["ENT_DATE"]                  = array(10,     "HISSU",    "DATE", "入学日");
            $checkdata["RELATIONSHIP"]              = array(2,      "HISSU",    "INT2", "続柄");
            $checkdata["GUARD_NAME"]                = array(120,    "",         "",     "保護者氏名");
            $checkdata["GUARD_KANA"]                = array(240,    "",         "",     "保護者氏名かな");
            $checkdata["GUARD_SEX"]                 = array(1,      "",         "INT",  "性別");
            $checkdata["GUARD_BIRTHDAY"]            = array(10,     "",         "DATE", "生年月日");
            $checkdata["GUARD_ZIPCD"]               = array(8,      "",         "",     "郵便番号");
            $checkdata["GUARD_ADDR1"]               = array(150,    "",         "",     "住所1");
            $checkdata["GUARD_ADDR2"]               = array(150,    "",         "",     "住所2");
            $checkdata["GUARD_TELNO"]               = array(14,     "",         "",     "電話番号");
            $checkdata["GUARD_FAXNO"]               = array(14,     "",         "",     "FAX番号");
            $checkdata["GUARD_E_MAIL"]              = array(50,     "",         "",     "E-MAIL");
            $checkdata["GUARD_JOBCD"]               = array(2,      "",         "INT2", "職種コード");
            $checkdata["GUARD_WORK_NAME"]           = array(120,    "",         "",     "勤務先名称");
            $checkdata["GUARD_WORK_TELNO"]          = array(14,     "",         "",     "勤務先電話番号");
            $checkdata["GUARANTOR_RELATIONSHIP"]    = array(2,      "",         "INT2", "保証人続柄");
            $checkdata["GUARANTOR_NAME"]            = array(120,    "",         "",     "保証人氏名");
            $checkdata["GUARANTOR_KANA"]            = array(240,    "",         "",     "保証人氏名かな");
            $checkdata["GUARANTOR_SEX"]             = array(1,      "",         "INT",  "保証人性別");
            $checkdata["GUARANTOR_ZIPCD"]           = array(8,      "",         "",     "保証人郵便番号");
            $checkdata["GUARANTOR_ADDR1"]           = array(150,    "",         "",     "保証人住所1");
            $checkdata["GUARANTOR_ADDR2"]           = array(150,    "",         "",     "保証人住所2");
            $checkdata["GUARANTOR_TELNO"]           = array(14,     "",         "",     "保証人電話番号");
            $checkdata["GUARANTOR_JOBCD"]           = array(2,      "",         "INT2", "保証人職種コード");
            $checkdata["PUBLIC_OFFICE"]             = array(30,     "",         "",     "兼ねている公職");
            $checkdata[$this->lastColumn]           = array(10,     "HISSU",    "",     $this->lastColumn);

            $db = Query::dbCheckOut();
            $db->autoCommit(false);

            //エラーデータの削除
            $query = $this->deleteQueryErr(PROGRAMID);
            $result = $db->query($query);

            $fp = fopen($this->file["tmp_name"], "r");
            $i  = 0;
            $line = 1;  //行数
            $header = false;
            $primaryKey1 = array();
            $primaryKey2 = array();
            $primaryKey3 = array();
            $duplicateCsv1 = array();
            $duplicateCsv2 = array();
            $duplicateCsv3 = array();
            $primaryKey1 = $this->getPrimaryKey('NYUGAKU_LIST_ANYTIME_DAT');
            $primaryKey2 = $this->getPrimaryKey('GUARDIAN_DAT');
            $primaryKey3 = $this->getPrimaryKey('GUARDIAN_ADDRESS_DAT');
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

                    //配列の要素数チェック
                    $checkcount = get_count($tmp);

                    $dataCnt = 0;
                    $data = array();
                    $data["ENTERYEAR"]              = $tmp[$dataCnt++];
                    $dataCnt += 4;
                    $data["SCHREGNO"]               = $tmp[$dataCnt++];
                    $data["ENT_DATE"]               = str_replace("/", "-", $tmp[$dataCnt++]);
                    $data["RELATIONSHIP"]           = $tmp[$dataCnt++];
                    $data["GUARD_NAME"]             = $tmp[$dataCnt++];
                    $data["GUARD_KANA"]             = $tmp[$dataCnt++];
                    $data["GUARD_SEX"]              = $tmp[$dataCnt++];
                    $data["GUARD_BIRTHDAY"]         = str_replace("/", "-", $tmp[$dataCnt++]);
                    $data["GUARD_ZIPCD"]            = $tmp[$dataCnt++];
                    $data["GUARD_ADDR1"]            = $tmp[$dataCnt++];
                    $data["GUARD_ADDR2"]            = $tmp[$dataCnt++];
                    $data["GUARD_TELNO"]            = $tmp[$dataCnt++];
                    $data["GUARD_FAXNO"]            = $tmp[$dataCnt++];
                    $data["GUARD_E_MAIL"]           = $tmp[$dataCnt++];
                    $data["GUARD_JOBCD"]            = $tmp[$dataCnt++];
                    $data["GUARD_WORK_NAME"]        = $tmp[$dataCnt++];
                    $data["GUARD_WORK_TELNO"]       = $tmp[$dataCnt++];
                    $data["GUARANTOR_RELATIONSHIP"] = $tmp[$dataCnt++];
                    $data["GUARANTOR_NAME"]         = $tmp[$dataCnt++];
                    $data["GUARANTOR_KANA"]         = $tmp[$dataCnt++];
                    $data["GUARANTOR_SEX"]          = $tmp[$dataCnt++];
                    $data["GUARANTOR_ZIPCD"]        = $tmp[$dataCnt++];
                    $data["GUARANTOR_ADDR1"]        = $tmp[$dataCnt++];
                    $data["GUARANTOR_ADDR2"]        = $tmp[$dataCnt++];
                    $data["GUARANTOR_TELNO"]        = $tmp[$dataCnt++];
                    $data["GUARANTOR_JOBCD"]        = $tmp[$dataCnt++];
                    $data["PUBLIC_OFFICE"]          = $tmp[$dataCnt++];
                    $data[$this->lastColumn]        = $tmp[$dataCnt++];

                    $data1 = array();
                    $data1["ISSUEDATE"]            = $this->field["GUARD_ISSUEDATE"];
                    $data1 = array_merge($data, $data1);

                    //項目数によるファイルチェック
                    if ($checkcount != $dataCnt) {
                        $this->warning = "CSVファイルを確認して下さい。\\nフォーマットが一致していません。";
                        return;
                    }

                    //エラーチェック
                    if ($this->errCheck2($db, $data, $checkdata, $line, $duplicateCsv1, $duplicateCsv2, $duplicateCsv3, $primaryKey1, $primaryKey2, $primaryKey3, $data1)) {
                        $temp_arr[$i] = $data;
                        $i++;
                    }
                    foreach ($primaryKey1 as $key => $val) {
                        $duplicateCsv1[$line][$val] = $data1[$val];
                    }
                    foreach ($primaryKey2 as $key => $val) {
                        $duplicateCsv2[$line][$val] = $data1[$val];
                    }
                    foreach ($primaryKey3 as $key => $val) {
                        $duplicateCsv3[$line][$val] = $data1[$val];
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
    public function errCheck2($db, $data, $data_each, $line, $duplicateCsv1, $duplicateCsv2, $duplicateCsv3, $primaryKey1, $primaryKey2, $primaryKey3, $data1)
    {
        $returnFlg          = true;
        $hissuErrFlg        = false;    //必須エラーフラグ
        $masterErrFlg       = false;    //存在エラーフラグ
        $numericErrFlg      = false;    //数字エラーフラグ
        $lengthErrFlg       = false;    //レングスエラーフラグ
        $otherErrFlg        = false;    //その他エラーフラグ
        $duplicateCsvErrFlg = false;    //重複(CSV)エラーフラグ
        $lastColErrFlg      = false;    //最終列文字エラーフラグ

        $hissuErrMsg        = "";       //必須エラーメッセージ
        $masterErrMsg       = "";       //存在エラーメッセージ
        $numericErrMsg      = "";       //数字エラーメッセージ
        $lengthErrMsg       = "";       //レングスエラーメッセージ
        $otherErrMsg        = "";       //その他エラーメッセージ
        $duplicateCsvErrMsg = "";       //重複(CSV)メッセージ
        $lastColErrMsg      = "";       //最終列文字エラーメッセージ

        $hissuSep        = "";
        $masterSep       = "";
        $numericSep      = "";
        $lengthSep       = "";
        $otherSep        = "";
        $duplicateCsvSep = "";
        $lastColSep      = "";

        $sonzaiarray = array("RELATIONSHIP"             => "H201",
                             "GUARD_SEX"                => "Z002",
                             "GUARD_JOBCD"              => "H202",
                             "GUARANTOR_RELATIONSHIP"   => "H201",
                             "GUARANTOR_SEX"            => "Z002",
                             "GUARANTOR_JOBCD"          => "H202");

        $dupFlg1 = $this->duplicateCsvCheck($duplicateCsv1, $primaryKey1, $data1);
        if ($dupFlg1[0] == 1) {
            $returnFlg = $this->errSet($duplicateCsvErrFlg, $duplicateCsvErrMsg, "※重複(CSV):", $duplicateCsvSep, "重複1(". $dupFlg1[1] ."行目)");
        }

        $dupFlg2 = $this->duplicateCsvCheck($duplicateCsv2, $primaryKey2, $data1);
        if ($dupFlg2[0] == 1) {
            $returnFlg = $this->errSet($duplicateCsvErrFlg, $duplicateCsvErrMsg, "※重複(CSV):", $duplicateCsvSep, "重複2(". $dupFlg2[1] ."行目)");
        }

        $dupFlg3 = $this->duplicateCsvCheck($duplicateCsv3, $primaryKey3, $data1);
        if ($dupFlg3[0] == 1) {
            $returnFlg = $this->errSet($duplicateCsvErrFlg, $duplicateCsvErrMsg, "※重複(CSV):", $duplicateCsvSep, "重複3(". $dupFlg3[1] ."行目)");
        }

        foreach ($data_each as $key => $val) {
            // 更新処理
            if ($this->field["SHORI_MEI"] == "1") {
                //必須項目チェック
                if ($val[1] == "HISSU" && !strlen(trim($data[$key]))) {
                    $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[3]);
                }
                //チェック用にゼロ埋め
                if ($val[2] == "INT2" && $data[$key] != "" && is_numeric($data[$key])) {
                    $data[$key] = sprintf("%0{$val[0]}d", $data[$key]);
                }
                //数値チェック
                if (($val[2] == "INT" || $val[2] == "INT2") && $data[$key] != "" && !is_numeric($data[$key])) {
                    $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, $val[3]);
                }
                //LASTCOLUMNの記載がない場合
                if ($key == $this->lastColumn && strlen(trim($data[$key])) != 0 && $data[$key] !== $this->lastColumn) {
                    $returnFlg = $this->errSet($lastColErrFlg, $lastColErrMsg, "※最終列文字不正:", $lastColSep, $this->lastColumn."でなければならない");
                }

                //名称マスタ
                if ($sonzaiarray[$key] && $data[$key] != "") {
                    if (0 == knja050hQuery::getNameMst($db, $this->field["YEAR"], $sonzaiarray[$key], $data[$key])) {
                        $returnFlg = $this->errSet($masterErrFlg, $masterErrMsg, "※マスタチェック:", $masterSep, $val[3]);
                    }
                }

                //日付型チェック
                if ($val[2] == "DATE") {
                    if (common::isDate($data[$key]) == false) {
                        $returnFlg = $this->errSet($otherErrFlg, $otherErrMsg, "※日付(フォーマット):", $otherSep, $val[3]);
                    }
                }

                //年度が画面と異なる場合
                if ($key == "ENTERYEAR" && $this->field["YEAR"] != $data[$key]) {
                    $returnFlg = $this->errSet($otherErrFlg, $otherErrMsg, "※その他:", $otherSep, "年度不正".$val[3]);
                }

                //文字数チェック
                if (strlen($data[$key]) > $val[0]) {
                    $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[3]);
                }
            } else {
                //必須項目チェック
                if (($key == "SCHREGNO" || $key == "ENT_DATE") && !strlen(trim($data[$key]))) {
                    $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[3]);
                }
                //チェック用にゼロ埋め
                if ($key == "SCHREGNO" && $data[$key] != "" && is_numeric($data[$key])) {
                    $data[$key] = sprintf("%0{$val[0]}d", $data[$key]);
                }
                //数値チェック
                if (($key == "SCHREGNO") && $data[$key] != "" && !is_numeric($data[$key])) {
                    $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, $val[3]);
                }
                //日付型チェック
                if ($key == "ENT_DATE") {
                    if (common::isDate($data[$key]) == false) {
                        $returnFlg = $this->errSet($otherErrFlg, $otherErrMsg, "※日付(フォーマット):", $otherSep, $val[3]);
                    }
                }
                //年度が画面と異なる場合
                if ($key == "ENTERYEAR" && $this->field["YEAR"] != $data[$key]) {
                    $returnFlg = $this->errSet($otherErrFlg, $otherErrMsg, "※その他:", $otherSep, "年度不正".$val[3]);
                }
                //LASTCOLUMNの記載がない場合
                if ($key == $this->lastColumn && strlen(trim($data[$key])) != 0 && $data[$key] !== $this->lastColumn) {
                    $returnFlg = $this->errSet($lastColErrFlg, $lastColErrMsg, "※最終列文字不正:", $lastColSep, $this->lastColumn."でなければならない");
                }

                //学籍番号と入学日が一致している場合のみ削除可能
                if (($key == "SCHREGNO") && common::isDate($data["ENT_DATE"]) == true) {
                    if (!knja050hQuery::schregnoExistBase($this, $data["SCHREGNO"], $data["ENT_DATE"], $db)) {
                        $returnFlg = $this->errSet($masterErrFlg, $masterErrMsg, "※マスタチェック:", $masterSep, $val[3]);
                    }
                }
                if (($key == "SCHREGNO") && !knja050hQuery::getGuardianDat($db, $schregno)) {
                    $returnFlg = $this->errSet($masterErrFlg, $masterErrMsg, "※マスタチェック:", $masterSep, $val[3]);
                }
                if (($key == "SCHREGNO") && !knja050hQuery::getGuardianAddressDat($db, $schregno)) {
                    $returnFlg = $this->errSet($masterErrFlg, $masterErrMsg, "※マスタチェック:", $masterSep, $val[3]);
                }
            }
        }

        if (!$returnFlg) {
            $checkError = $duplicateCsvErrMsg.$hissuErrMsg.$numericErrMsg.$lengthErrMsg.$otherErrMsg.$masterErrMsg.$lastColErrMsg;
            $checkError = $this->checkErrLength($checkError);
            knja050hQuery::insertQueryErr($db, $line, $checkError);
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

    /********************/
    /*  ＣＳＶ出力処理  */
    /********************/
    //テンプレートの書き出し
    public function outputTmpFile()
    {
        $header_data = true;   //出力フラグ

        //エラー項目
        $header_err = array("MSGROW"        => "行番号",
                            "MSGREMARK"     => "メッセージ");

        /**********************/
        /*  ヘッダー部分出力  */
        /**********************/
        if ($this->field["OUTPUT"] == "1") {
            // 入学者
            if ($this->field["DATADIV"] == 1) {
                $header = array();
                $header["ENTEXAMYEAR"]          = "※入学年度";
                $header["SEMESTER"]             = "※学期";
                $header["SCHREGNO"]             = "※学籍番号";
                $header["ENT_DATE"]             = "※入学日";
                $header["ENT_DIV"]              = "※入学区分";
                $header["GRADE"]                = "※学年";
                $header["HR_CLASS"]             = "※組";
                $header["ATTENDNO"]             = "※出席番号";
                $header["INOUTCD"]              = "※内外区分";
                $header["COURSECD"]             = "※課程コード";
                $header["MAJORCD"]              = "※学科コード";
                $header["COURSECODE"]           = "コースコード";
                $header["NAME"]                 = "※氏名";
                $header["NAME_KANA"]            = "氏名かな";
                $header["BIRTHDAY"]             = "生年月日";
                $header["SEX"]                  = "性別";
                $header["FS_CD"]                = "出身学校コード";
                $header["FINSCHOOLGRADDATE"]    = "出身学校卒業日";
                $header["PRISCHOOLCD"]          = "塾コード";
                $header["DORMITORY_FLG"]        = "寮生フラグ";
                $header["ZIPCD"]                = "郵便番号";
                $header["ADDRESS1"]             = "住所１";
                $header["ADDRESS2"]             = "住所２";
                $header["TELNO"]                = "電話番号";
                $header["FAXNO"]                = "FAX番号";
                $header["EMAIL"]                = "EMAIL";
                $header["EMERGENCYCALL"]        = "緊急連絡先";
                $header["EMERGENCYTELNO"]       = "緊急連絡電話番号";
                $header["SCALASHIPDIV"]         = "スカラシップ区分";
                $header["KATEI_ENTEXAMYEAR"]    = "課程入学年度";
                $header["TIKUCD"]               = "地区コード";
                $header["EXAMNO"]               = "受験番号";
                $header["TESTDIV"]              = "入試種別";
                $header["EXAM_TYPE"]            = "入試方式";
                $header[$this->lastColumn]      = $this->lastColumn;

                $header_rei = array();
                $header_rei["ENTEXAMYEAR"]          = CTRL_YEAR;
                $header_rei["SCHREGNO"]             = "99999999";
                $header_rei["ENT_DATE"]             = CTRL_DATE;
                $header_rei["ENT_DIV"]              = "1";
                $header_rei["GRADE"]                = "01";
                $header_rei["HR_CLASS"]             = "001";
                $header_rei["ATTENDNO"]             = "001";
                $header_rei["INOUTCD"]              = "1";
                $header_rei["COURSECD"]             = "1";
                $header_rei["MAJORCD"]              = "001";
                $header_rei["COURSECODE"]           = "0001";
                $header_rei["NAME"]                 = "山田太郎";
                $header_rei["NAME_KANA"]            = "やまだたろう";
                $header_rei["BIRTHDAY"]             = "1980-10-20";
                $header_rei["SEX"]                  = "1";
                $header_rei["FS_CD"]                = "9999999";
                $header_rei["FINSCHOOLGRADDATE"]    = (CTRL_YEAR - 1)."-03-31";
                $header_rei["PRISCHOOLCD"]          = "9999999";
                $header_rei["DORMITORY_FLG"]        = "1";
                $header_rei["ZIPCD"]                = "801-2211";
                $header_rei["ADDRESS1"]             = "東京都○○市○○町";
                $header_rei["ADDRESS2"]             = "一丁目２－３";
                $header_rei["TELNO"]                = "04-8765-4321";
                $header_rei["FAXNO"]                = "03-1234-5678";
                $header_rei["EMAIL"]                = "yamada@yamada.co.jp";
                $header_rei["EMERGENCYCALL"]        = "株式会社○○";
                $header_rei["EMERGENCYTELNO"]       = "03-1234-5678";
                $header_rei["SCALASHIPDIV"]         = "99";
                $header_rei["KATEI_ENTEXAMYEAR"]    = CTRL_YEAR;
                $header_rei["TIKUCD"]               = "99";
                $header_rei["EXAMNO"]               = "99999";
                $header_rei["TESTDIV"]              = "01";
                $header_rei["EXAM_TYPE"]            = "01";
                $header_rei[$this->lastColumn]      = $this->lastColumn;

            // 保護者
            } else {
                $header = array();
                $header["GRADE"]                    = "学年";
                $header["HR_CLASS"]                 = "クラス";
                $header["ATTENDNO"]                 = "出席番号";
                $header["NAME"]                     = "氏名";
                $header["SCHREGNO"]                 = "※学籍番号";
                $header["ENT_DATE"]                 = "※入学日";
                $header["RELATIONSHIP"]             = "※続柄";
                $header["GNAME"]                    = "保護者氏名";
                $header["GKANA"]                    = "保護者氏名かな";
                $header["SEITO_SEX"]                = "性別";
                $header["BIRTHDAY"]                 = "生年月日";
                $header["GZIPCD"]                   = "郵便番号";
                $header["GADDRESS1"]                = "住所1";
                $header["GADDRESS2"]                = "住所2";
                $header["GTELNO"]                   = "電話番号";
                $header["GFAXNO"]                   = "FAX番号";
                $header["EMAIL"]                    = "E-MAIL";
                $header["GUARD_JOBCD"]              = "職種コード";
                $header["GUARD_WORK_NAME"]          = "勤務先名称";
                $header["GUARD_WORK_TELNO"]         = "勤務先電話番号";
                $header["GUARANTOR_RELATIONSHIP"]   = "保証人続柄";
                $header["GUARANTOR_NAME"]           = "保証人氏名";
                $header["GUARANTOR_KANA"]           = "保証人氏名かな";
                $header["GUARANTOR_SEX"]            = "保証人性別";
                $header["GUARANTOR_ZIPCD"]          = "保証人郵便番号";
                $header["GUARANTOR_ADDR1"]          = "保証人住所1";
                $header["GUARANTOR_ADDR2"]          = "保証人住所2";
                $header["GUARANTOR_TELNO"]          = "保証人電話番号";
                $header["GUARANTOR_JOBCD"]          = "保証人職種コード";
                $header["PUBLIC_OFFICE"]            = "兼ねている公職";
                $header[$this->lastColumn]          = $this->lastColumn;

                $header_rei = array();
                $header_rei["GRADE"]                    = "99";
                $header_rei["HR_CLASS"]                 = "999";
                $header_rei["ATTENDNO"]                 = "999";
                $header_rei["NAME"]                     = "アルプ太郎";
                $header_rei["SCHREGNO"]                 = "12345678";
                $header_rei["ENT_DATE"]                 = CTRL_DATE;
                $header_rei["RELATIONSHIP"]             = "99";
                $header_rei["GUARD_NAME"]               = "全角20";
                $header_rei["GUARD_KANA"]               = "全角40";
                $header_rei["GUARD_SEX"]                = "9";
                $header_rei["GUARD_BIRTHDAY"]           = "9999-99-99";
                $header_rei["GUARD_ZIPCD"]              = "999-9999";
                $header_rei["GUARD_ADDR1"]              = "全角30";
                $header_rei["GUARD_ADDR2"]              = "全角30";
                $header_rei["GUARD_TELNO"]              = "9999-9999-9999";
                $header_rei["GUARD_FAXNO"]              = "9999-9999-9999";
                $header_rei["GUARD_E_MAIL"]             = "aaa123@bbb.co.jp";
                $header_rei["GUARD_JOBCD"]              = "99";
                $header_rei["GUARD_WORK_NAME"]          = "全角20";
                $header_rei["GUARD_WORK_TELNO"]         = "9999-9999-9999";
                $header_rei["GUARANTOR_RELATIONSHIP"]   = "99";
                $header_rei["GUARANTOR_NAME"]           = "全角20";
                $header_rei["GUARANTOR_KANA"]           = "全角40";
                $header_rei["GUARANTOR_SEX"]            = "9";
                $header_rei["GUARANTOR_ZIPCD"]          = "999-9999";
                $header_rei["GUARANTOR_ADDR1"]          = "全角30";
                $header_rei["GUARANTOR_ADDR2"]          = "全角30";
                $header_rei["GUARANTOR_TELNO"]          = "9999-9999-9999";
                $header_rei["GUARANTOR_JOBCD"]          = "99";
                $header_rei["PUBLIC_OFFICE"]            = "全角10";
                $header_rei[$this->lastColumn]          = $this->lastColumn;
            }
        }

        if ($this->field["OUTPUT"] == "1") {
            //ヘッダ
            $contents .= implode($header, ",")."\n";
            $contents .= implode($header_rei, ",")."\n";
        } elseif ($this->field["OUTPUT"] == "3") {
            //エラー
            $headerDuplicate = array("SCHREGNO"    => "学籍番号",
                                     "ISSUEDATE"   => "有効期間開始日付",
                                     "YEAR"        => "学期",
                                     "SEMESTER"    => "住所開始日",
                                     "DOMI_ENTDAY" => "入寮日",
                                     "BASE_SEQ"    => "学籍基礎SEQ",
                                     "ENTERYEAR"   => "入学年度");
            $tables = array();
            if ($this->field["DATADIV"] == 2) {
                $tables = array("1" => "NYUGAKU_LIST_ANYTIME_DAT",
                                "2" => "GUARDIAN_DAT",
                                "3" => "GUARDIAN_ADDRESS_DAT");
            } else {
                $tables = array("1" => "NYUGAKU_LIST_ANYTIME_DAT",
                                "2" => "SCHREG_BASE_MST",
                                "3" => "SCHREG_BASE_DETAIL_MST",
                                "4" => "SCHREG_REGD_DAT",
                                "5" => "SCHREG_ADDRESS_DAT",
                                "6" => "SCHREG_DOMITORY_HIST_DAT");
            }
            $dupTable = $this->getDuplicateErrorHeader(PROGRAMID, $tables);
            if (sizeof($dupTable) > 0) {
                $headerErr = $this->duplicateCsvHeader($dupTable, $headerDuplicate);
                $contents .= implode($headerErr, ",")."\n";
            }
            $contents .= implode($header_err, ",")."\n";
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
}
?>
