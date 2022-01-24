<?php

require_once('for_php7.php');
class knjx091Model extends Model
{
    public $field      = array();
    public $data       = array();
    public $cmd;
    public $file;
    public $control; //コントロールマスタ
    public $staffcd; //登録者コード
    public $fieldSize = array();
    public $lastColumn;//CSVの最後の列の値

    public function init()
    {
        common::GetControlMaster_Fnc($this->control);

        $this->field = array("OUTPUT"           =>  VARS::post("OUTPUT"),           //出力取込種別
                             "FORM"             =>  VARS::post("FORM"),             //出力方式
                             "YEAR_GRADE"       =>  VARS::post("YEAR_GRADE"),       //年度・学年
                             "YEAR_SEMESTER"    =>  VARS::post("YEAR_SEMESTER"),    //年度_学期
                             "GRADE"            =>  VARS::post("GRADE"),            //学年
                             "SUBCLASS"         =>  VARS::post("SUBCLASS"),         //科目
                             "HEADER"           =>  VARS::post("HEADER"),           //ヘッダ有
                             "SHORI_MEI"        =>  VARS::post("SHORI_MEI")         //処理名
                             );

        $this->cmd = VARS::request("cmd");

        if (VARS::request("cmd") != "") {
            list($this->field["YEAR"], $this->field["SEMESTER"]) = explode('-', VARS::post("YEAR_SEMESTER"));
        } else {
            $this->field["YEAR"]     = CTRL_YEAR;
            $this->field["SEMESTER"] = CTRL_SEMESTER;
        }

        if (VARS::file("FILE")) {
            $this->field["FILE"] = VARS::file("FILE");        //ファイルのアップロード
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

    /***************/
    /* CSVの取込み */
    /***************/
    public function &getExecModel()
    {
        //1科目／1行
        if ($this->field["FORM"] == "1") {
            //ファイルの文字列の取得
            $this->data_arr = array();
            $this->data_arr = $this->getFileString();

            //データの整合性チェック
            if ($this->isDataMatched($this->data_arr)) {
                $errFlg = $this->checkCsvErrQuery(PROGRAMID);
                if ($errFlg) {
                    if ($this->field["SHORI_MEI"] == "1") {
                        knjx091Query::deleteQueryCsv($this, $this->data_arr); //処理件数を返すけれども使わない。とりあえず代入しただけ。
                        knjx091Query::insertQueryCsv($this, $this->data_arr);
                    } else {
                        knjx091Query::deleteQueryCsv($this, $this->data_arr);

                        //GROUPCDが600以上で履修生徒がいないとき削除
                        knjx091Query::getDelete600Query($this, $this->data_arr);
                    }
                    $this->setMessage("MSG202", "更新件数".get_count($this->data_arr)."件");
                }
            }

            //複数科目／1行
        } else {
            //ファイルの文字列の取得
            $this->data_arrH = $this->data_arr = array();
            list($this->data_arrH, $this->data_arr) = $this->getFileString2();

            //データの整合性チェック
            if ($this->isDataMatched($this->data_arr)) {
                $errFlg = $this->checkCsvErrQuery(PROGRAMID);
                if ($errFlg) {
                    if ($this->field["SHORI_MEI"] == "1") {
                        knjx091Query::deleteQueryCsv2($this, $this->data_arrH, $this->data_arr); //処理件数を返すけれども使わない。とりあえず代入しただけ。
                        knjx091Query::insertQueryCsv2($this, $this->data_arrH, $this->data_arr);
                    } else {
                        knjx091Query::deleteQueryCsv2($this, $this->data_arrH, $this->data_arr);
                    }
                    $this->setMessage("MSG202", "更新件数".get_count($this->data_arr)."件");
                }
            }
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
            if ($this->Properties["useCurriculumcd"] == '1') {
                $checkdata = array("YEAR"          => array(4,"年度"),
                                   "SEMESTER"      => array(1,"学期"),
                                   "COURSECD"      => array(1,"課程"),
                                   "MAJORCD"       => array(3,"学科"),
                                   "COURSECODE"    => array(4,"コース"),
                                   "GROUPCD"       => array(3,"選択グループコード"),
                                   "CLASSCD"       => array(2,"教科コード"),
                                   "SCHOOL_KIND"   => array(1,"学校種別"),
                                   "CURRICULUM_CD" => array(1,"教育課程コード"),
                                   "SUBCLASSCD"    => array(6,"科目コード"),
                                   "GRADE"         => array(2,"学年"),
                                   "SCHREGNO"      => array(8,"学籍番号"));
            } else {
                $checkdata = array("YEAR"          => array(4,"年度"),
                                   "SEMESTER"      => array(1,"学期"),
                                   "COURSECD"      => array(1,"課程"),
                                   "MAJORCD"       => array(3,"学科"),
                                   "COURSECODE"    => array(4,"コース"),
                                   "GROUPCD"       => array(3,"選択グループコード"),
                                   "CLASSCD"       => array(2,"教科コード"),
                                   "CURRICULUM_CD" => array(1,"教育課程コード"),
                                   "SUBCLASSCD"    => array(6,"科目コード"),
                                   "GRADE"         => array(2,"学年"),
                                   "SCHREGNO"      => array(8,"学籍番号"));
            }

            $db = Query::dbCheckOut();
            $db->autoCommit(false);

            //エラーデータの削除
            $query = $this->deleteQueryErr(PROGRAMID);
            $result = $db->query($query);

            $fp = fopen($this->field["FILE"]["tmp_name"], "r");
            $i  = 0;
            $line = 1;  //行数
            $header = false;

            //取得主キー
            $primaryKey1    = array();
            $primaryKey2    = array();
            $duplicateCsv1  = array();
            $duplicateCsv2  = array();
            $primaryKey1    = $this->getPrimaryKey('SUBCLASS_STD_SELECT_DAT');
            if ($this->field["SHORI_MEI"] == "2") {
                $primaryKey2 = $this->getPrimaryKey('SUBCLASS_COMP_SELECT_DAT');
            }
            while (!feof($fp)) {
                $buffer = fgets($fp, 4096);
                if (trim($buffer) != '') {
                    if (!$header && $this->field["HEADER"] == "on") {
                        $header = true;
                        $line++;
                        continue;
                    }
                    //文字コードを判別、改行コードを削除
                    $buffer = trim(mb_convert_encoding($buffer, "SJIS-win", mb_http_output()));
                    $tmp    = common::csv2array($buffer);

                    if ($this->Properties["useCurriculumcd"] == '1') {
                        $data = array("YEAR"           => (strlen(trim($tmp[0])))  ? sprintf("%04d", $tmp[0])  : "",
                                      "SEMESTER"       => (strlen(trim($tmp[1])))  ? sprintf("%01d", $tmp[1])  : "",
                                      "COURSECD"       => (strlen(trim($tmp[2])))  ? sprintf("%01d", $tmp[2])  : "",
                                      "MAJORCD"        => (strlen(trim($tmp[4])))  ? sprintf("%03d", $tmp[4])  : "",
                                      "COURSECODE"     => (strlen(trim($tmp[6])))  ? sprintf("%04d", $tmp[6])  : "",
                                      "GRADE"          => (strlen(trim($tmp[7])))  ? sprintf("%02d", $tmp[7])  : "",
                                      "SCHREGNO"       => (strlen(trim($tmp[11]))) ? sprintf("%08d", $tmp[11]) : "",
                                      "CLASSCD"        => (strlen(trim($tmp[13]))) ? sprintf("%02d", $tmp[13])  : "",
                                      "SCHOOL_KIND"    => (strlen(trim($tmp[15]))) ? $tmp[15]  : "",
                                      "CURRICULUM_CD"  => (strlen(trim($tmp[16]))) ? sprintf("%01d", $tmp[16])  : "",
                                      "SUBCLASSCD"     => (strlen(trim($tmp[17]))) ? sprintf("%06d", $tmp[17])  : "",
                                      "GROUPCD"        => (strlen(trim($tmp[19]))) ? sprintf("%03d", $tmp[19])  : "");
                    } else {
                        $data = array("YEAR"           => (strlen(trim($tmp[0])))  ? sprintf("%04d", $tmp[0])  : "",
                                      "SEMESTER"       => (strlen(trim($tmp[1])))  ? sprintf("%01d", $tmp[1])  : "",
                                      "COURSECD"       => (strlen(trim($tmp[2])))  ? sprintf("%01d", $tmp[2])  : "",
                                      "MAJORCD"        => (strlen(trim($tmp[4])))  ? sprintf("%03d", $tmp[4])  : "",
                                      "COURSECODE"     => (strlen(trim($tmp[6])))  ? sprintf("%04d", $tmp[6])  : "",
                                      "GRADE"          => (strlen(trim($tmp[7])))  ? sprintf("%02d", $tmp[7])  : "",
                                      "SCHREGNO"       => (strlen(trim($tmp[11]))) ? sprintf("%08d", $tmp[11]) : "",
                                      "CLASSCD"        => (strlen(trim($tmp[13]))) ? sprintf("%02d", $tmp[13])  : "",
                                      "CURRICULUM_CD"  => (strlen(trim($tmp[15]))) ? sprintf("%01d", $tmp[15])  : "",
                                      "SUBCLASSCD"     => (strlen(trim($tmp[16]))) ? sprintf("%06d", $tmp[16])  : "",
                                      "GROUPCD"        => (strlen(trim($tmp[18]))) ? sprintf("%03d", $tmp[18])  : "");
                    }

                    //エラーチェック
                    if ($this->errCheck($db, $data, $checkdata, $line, $duplicateCsv1, $duplicateCsv2, $primaryKey1, $primaryKey2)) {
                        $temp_arr[$i] = $data;
                        $i++;
                    }
                    foreach ($primaryKey1 as $key => $val) {
                        $duplicateCsv1[$line][$val] = $data[$val];
                    }
                    if ($this->field["SHORI_MEI"] == "2") {
                        foreach ($primaryKey2 as $key => $val) {
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

    /******************/
    /* エラーチェック */
    /******************/
    public function errCheck($db, $data, $checkdata, $line, $duplicateCsv1, $duplicateCsv2, $primaryKey1, $primaryKey2)
    {
        $returnFlg          = true;
        $hissuErrFlg        = false;    //必須エラーフラグ
        $masterErrFlg       = false;    //マスタエラーフラグ
        $numericErrFlg      = false;    //数字エラーフラグ
        $lengthErrFlg       = false;    //レングスエラーフラグ
        $duplicateCsvErrFlg = false;    //重複(CSV)エラーフラグ

        $hissuErrMsg        = "";       //必須エラーメッセージ
        $masterErrMsg       = "";       //マスタエラーメッセージ
        $numericErrMsg      = "";       //数字エラーメッセージ
        $lengthErrMsg       = "";       //レングスエラーメッセージ
        $duplicateCsvErrMsg = "";       //重複(CSV)メッセージ

        $hissuSep        = "";
        $masterSep       = "";
        $numericSep      = "";
        $lengthSep       = "";
        $duplicateCsvSep = "";

        //学校種別
        $school_kind = $db->getCol(knjx091Query::getSchoolKind());
        //教育課程
        $curriculum_cd = $db->getCol(knjx091Query::getCurriculumCd());

        //重複(CSV)チェック
        $dupFlg1 = $this->duplicateCsvCheck($duplicateCsv1, $primaryKey1, $data);
        if ($dupFlg1[0] == 1) {
            $returnFlg = $this->errSet($duplicateCsvErrFlg, $duplicateCsvErrMsg, "※重複(CSV):", $duplicateCsvSep, "重複1(". $dupFlg1[1] ."行目)");
        }

        if ($this->field["SHORI_MEI"] == "2") {
            $dupFlg2 = $this->duplicateCsvCheck($duplicateCsv2, $primaryKey2, $data);
            if ($dupFlg2[0] == 1) {
                $returnFlg = $this->errSet($duplicateCsvErrFlg, $duplicateCsvErrMsg, "※重複(CSV):", $duplicateCsvSep, "重複2(". $dupFlg2[1] ."行目)");
            }
        }
        foreach ($checkdata as $key => $val) {
            if ($this->Properties["useCurriculumcd"] == '1') {
                //必須項目チェック
                if (in_array($key, array("YEAR","SEMESTER","COURSECD","MAJORCD","COURSECODE","GROUPCD","CLASSCD","SCHOOL_KIND","CURRICULUM_CD","SUBCLASSCD","GRADE","SCHREGNO"))) {
                    if (!strlen(trim($data[$key]))) {
                        $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[1]);
                    } else {
                        if ($this->field["SHORI_MEI"] == "1") {
                            if ($key == 'SCHOOL_KIND' && !in_array($data[$key], $school_kind)) {
                                $returnFlg = $this->errSet($masterErrFlg, $masterErrMsg, "※マスタチェック:", $masterSep, $val[1]);
                            }
                            if ($key == 'CURRICULUM_CD' && !in_array($data[$key], $curriculum_cd)) {
                                $returnFlg = $this->errSet($masterErrFlg, $masterErrMsg, "※マスタチェック:", $masterSep, $val[1]);
                            }
                        }
                    }
                }
            } else {
                //必須項目チェック
                if (in_array($key, array("YEAR","SEMESTER","COURSECD","MAJORCD","COURSECODE","GROUPCD","CLASSCD","CURRICULUM_CD","SUBCLASSCD","GRADE","SCHREGNO"))) {
                    if (!strlen(trim($data[$key]))) {
                        $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[1]);
                    } else {
                        if ($this->field["SHORI_MEI"] == "1") {
                            if ($key == 'CLASSCD' && 0 == $db->getOne(knjx091Query::classcdCntSql($this, $data, $data[$key]))) {
                                $returnFlg = $this->errSet($masterErrFlg, $masterErrMsg, "※マスタチェック:", $masterSep, $val[1]);
                            }
                            if ($key == 'SUBCLASSCD' && 0 == $db->getOne(knjx091Query::subclassCntSql($this, $data, $data[$key]))) {
                                $returnFlg = $this->errSet($masterErrFlg, $masterErrMsg, "※マスタチェック:", $masterSep, $val[1]);
                            }
                        }
                    }
                }
            }

            //数値チェック
            if (in_array($key, array("YEAR","SEMESTER","COURSECD","MAJORCD","COURSECODE","GROUPCD","CLASSCD","CURRICULUM_CD","SUBCLASSCD","SCHREGNO"))) {
                if ($data[$key] != "" && !is_numeric($data[$key])) {
                    $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, $val[1]);
                }
            }

            if ($this->field["SHORI_MEI"] == "1") {
                //文字数チェック
                if (strlen($data[$key]) > $val[0]) {
                    $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[1]);
                }
            }
        }

        //マスタの存在チェック
        if ($this->Properties["useCurriculumcd"] == '1') {
            if ($this->field["SHORI_MEI"] == "1") {
                if (0 == $db->getOne(knjx091Query::classcdCntSql($this, $data, $data["CLASSCD"]))) {
                    $returnFlg = $this->errSet($masterErrFlg, $masterErrMsg, "※マスタチェック:", $masterSep, "教科コード");
                }
                if (0 == $db->getOne(knjx091Query::subclassCntSql($this, $data, $data["SUBCLASSCD"]))) {
                    $returnFlg = $this->errSet($masterErrFlg, $masterErrMsg, "※マスタチェック:", $masterSep, "科目コード");
                }
            }
        }

        //YEAR、SEMESTER、SCHREGNO の入力があった場合
        //SCHREG_REGD_DAT にその生徒が登録されているのかチェックする
        if (strlen(trim($data["YEAR"])) && strlen(trim($data["SEMESTER"])) && strlen(trim($data["SCHREGNO"]))) {
            if (0 == $db->getOne(knjx091Query::schregNoCntSql($data["YEAR"], $data["SEMESTER"], $data["SCHREGNO"], $data["COURSECD"], $data["MAJORCD"], $data["COURSECODE"]))) {
                $returnFlg = $this->errSet($masterErrFlg, $masterErrMsg, "※マスタチェック:", $masterSep, "学籍番号");
            }
        }

        //SUBCLASS_COMP_SELECT_DATの存在チェック
        if ($this->Properties["useCurriculumcd"] == '1') {
            if (strlen(trim($data["YEAR"])) && strlen(trim($data["GRADE"])) && strlen(trim($data["GROUPCD"])) && strlen(trim($data["CLASSCD"])) && strlen(trim($data["SUBCLASSCD"])) && strlen(trim($data["SCHOOL_KIND"]))) {
                if (0 == $db->getOne(knjx091Query::groupcdCntSql($data["YEAR"], $data["GRADE"], $data["COURSECD"], $data["MAJORCD"], $data["COURSECODE"], $data["GROUPCD"], $data["CLASSCD"], $data["SUBCLASSCD"], $data["SCHOOL_KIND"]))) {
                    $returnFlg = $this->errSet($masterErrFlg, $masterErrMsg, "※マスタチェック:", $masterSep, "選択グループコード");
                }
            }
        } else {
            if (strlen(trim($data["YEAR"])) && strlen(trim($data["GRADE"])) && strlen(trim($data["GROUPCD"])) && strlen(trim($data["CLASSCD"])) && strlen(trim($data["SUBCLASSCD"]))) {
                if (0 == $db->getOne(knjx091Query::groupcdCntSql($data["YEAR"], $data["GRADE"], $data["COURSECD"], $data["MAJORCD"], $data["COURSECODE"], $data["GROUPCD"], $data["CLASSCD"], $data["SUBCLASSCD"]))) {
                    $returnFlg = $this->errSet($masterErrFlg, $masterErrMsg, "※マスタチェック:", $masterSep, "選択グループコード");
                }
            }
        }
        if (!$returnFlg) {
            $checkError = $duplicateCsvErrMsg.$hissuErrMsg.$numericErrMsg.$lengthErrMsg.$masterErrMsg;
            $checkError = $this->checkErrLength($checkError);
            knjx091Query::insertQueryErr($db, $line, $checkError);
        }
        return $returnFlg;
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

    /****************************************/
    /* CSVファイルを配列へ（複数科目／1行） */
    /****************************************/
    public function getFileString2()
    {
        $temp_arrH = $temp_arr = array();
        if (!is_uploaded_file($this->field["FILE"]["tmp_name"])) { //HTTP POSTによりアップロードされたファイルかどうかを調べる
            $this->warning = "ファイル名が不正です。";
        } else {
            $checkdata1 = array("YEAR"          => array(4,"履修年度"),
                                "GRADE"         => array(2,"履修学年"),
                                "GROUPCD"       => array(14,"選択グループコード"),
                                "SUBCLASSCD"    => array(13,"科目コード"));

            $checkdata2 = array("YEAR"          => array(4,"年度"),
                                "SEMESTER"      => array(1,"学期"),
                                "COURSECD"      => array(1,"課程"),
                                "MAJORCD"       => array(3,"学科"),
                                "COURSECODE"    => array(4,"コース"),
                                "GRADE"         => array(2,"学年"),
                                "SCHREGNO"      => array(8,"学籍番号"));

            $db = Query::dbCheckOut();
            $db->autoCommit(false);

            //エラーデータの削除
            $query = $this->deleteQueryErr(PROGRAMID);
            $result = $db->query($query);

            $fp = fopen($this->field["FILE"]["tmp_name"], "r");
            $i  = 0;
            $line = 1;  //行数
            $header = false;
            $dataH = $data = array();

            //取得主キー
            $primaryKey1    = array();
            $duplicateCsv1  = array();
            $primaryKey1    = $this->getPrimaryKey('SUBCLASS_STD_SELECT_DAT');

            while (!feof($fp)) {
                $buffer = fgets($fp, 4096);
                if (trim($buffer) != '') {
                    //文字コードを判別、改行コードを削除
                    $buffer = trim(mb_convert_encoding($buffer, "SJIS-win", mb_http_output()));
                    $tmp    = common::csv2array($buffer);

                    if ($line == 1) {
                        $dataH["YEAR"]  = (strlen(trim($tmp[1]))) ? sprintf("%04d", $tmp[1]) : "";
                        $dataH["GRADE"] = (strlen(trim($tmp[3]))) ? sprintf("%02d", $tmp[3]) : "";
                        //選択グループ格納
                        for ($j = 13; $j < get_count($tmp); $j++) {
                            if ($tmp[$j] == $this->lastColumn) {
                                continue;
                            } else {
                                $dataH["GROUPCD"][] = (strlen(trim($tmp[$j]))) ? $tmp[$j] : "";
                            }
                        }
                        //エラーチェック
                        $this->errCheck2_1($db, $dataH, $checkdata1, $line);
                    } elseif ($line == 2 || $line == 4) {
                        $line++;
                        continue;
                    } elseif ($line == 3) {
                        //科目コード格納
                        for ($j = 13; $j < get_count($tmp); $j++) {
                            if ($tmp[$j] == $this->lastColumn) {
                                continue;
                            } else {
                                $dataH["SUBCLASSCD"][] = (strlen(trim($tmp[$j]))) ? $tmp[$j] : "";
                            }
                        }
                        //エラーチェック
                        $this->errCheck2_1($db, $dataH, $checkdata1, $line);
                    } else {
                        $data = array("YEAR"           => (strlen(trim($tmp[0])))  ? sprintf("%04d", $tmp[0])  : "",
                                      "SEMESTER"       => (strlen(trim($tmp[1])))  ? sprintf("%01d", $tmp[1])  : "",
                                      "COURSECD"       => (strlen(trim($tmp[2])))  ? sprintf("%01d", $tmp[2])  : "",
                                      "MAJORCD"        => (strlen(trim($tmp[4])))  ? sprintf("%03d", $tmp[4])  : "",
                                      "COURSECODE"     => (strlen(trim($tmp[6])))  ? sprintf("%04d", $tmp[6])  : "",
                                      "GRADE"          => (strlen(trim($tmp[7])))  ? sprintf("%02d", $tmp[7])  : "",
                                      "SCHREGNO"       => (strlen(trim($tmp[11]))) ? sprintf("%08d", $tmp[11]) : "");
                        //チェック格納
                        for ($j = 13; $j < get_count($tmp); $j++) {
                            if ($tmp[$j] == $this->lastColumn) {
                                continue;
                            } else {
                                $data["FLG"][] = (strlen(trim($tmp[$j]))) ? $tmp[$j] : "";
                            }
                        }
                        //エラーチェック
                        if ($this->errCheck2_2($db, $data, $checkdata2, $line, $dataH["SUBCLASSCD"], $duplicateCsv1, $primaryKey1)) {
                            $temp_arr[$i] = $data;
                            $i++;
                        }
                        foreach ($primaryKey1 as $key => $val) {
                            $duplicateCsv1[$line][$val] = $data[$val];
                        }
                    }
                    $line++;
                }
            }
            $temp_arrH = $dataH;

            $db->commit();  // トランザクションをコミットする。
            Query::dbCheckIn($db);
            fclose($fp);
        }

        return array($temp_arrH, $temp_arr);
    }

    /***********************************/
    /* エラーチェック（複数科目／1行） */
    /***********************************/
    //エラー表示（ポップアップ表示：ヘッダ）
    public function errCheck2_1($db, $data, $checkdata, $line)
    {
        $returnFlg       = true;
        $hissuErrFlg     = false;    //必須エラーフラグ
        $masterErrFlg    = false;    //マスタエラーフラグ
        $numericErrFlg   = false;    //数字エラーフラグ
        $lengthErrFlg    = false;    //レングスエラーフラグ
        $orderErrFlg     = false;    //異なるエラーフラグ

        $hissuErrMsg     = "";       //必須エラーメッセージ
        $masterErrMsg    = "";       //マスタエラーメッセージ
        $numericErrMsg   = "";       //数字エラーメッセージ
        $lengthErrMsg    = "";       //レングスエラーメッセージ
        $orderErrMsg     = "";       //異なるメッセージ

        $hissuSep     = "";
        $masterSep    = "";
        $numericSep   = "";
        $lengthSep    = "";
        $orderSep     = "";

        if ($line == 1) {
            foreach ($checkdata as $key => $val) {
                //必須項目チェック
                if (in_array($key, array("YEAR","GRADE"))) {
                    if (!strlen(trim($data[$key]))) {
                        $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[1]);
                    }
                }
                if ($key == "GROUPCD") {
                    if (!strlen(trim(implode('', $data[$key])))) {
                        $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[1]);
                    }
                }

                //数値チェック
                if (in_array($key, array("YEAR","GRADE"))) {
                    if ($data[$key] != "" && !is_numeric($data[$key])) {
                        $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, $val[1]);
                    }
                }

                if ($this->field["SHORI_MEI"] == "1") {
                    //文字数チェック
                    if ($key == "GROUPCD") {
                        for ($g = 0; $g < get_count($data[$key]); $g++) {
                            if (strlen($data[$key][$g]) > $val[0]) {
                                $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[1]);
                            }
                        }
                    } else {
                        if (strlen($data[$key]) > $val[0]) {
                            $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[1]);
                        }
                    }
                }
            }

            //項目チェック
            list($year, $grade) = explode('-', $this->field["YEAR_GRADE"]);
            if ($data["YEAR"] != $year) {
                $returnFlg = $this->errSet($orderErrFlg, $orderErrMsg, "※固有エラー:", $orderSep, "画面の履修年度と異なります。");
            }

            if ($data["GRADE"] != $grade) {
                $returnFlg = $this->errSet($orderErrFlg, $orderErrMsg, "※固有エラー:", $orderSep, "画面の学年と異なります。");
            }

            //SUBCLASS_COMP_SELECT_DATの存在チェック
            if (strlen(trim($data["YEAR"])) && strlen(trim($data["GRADE"])) && strlen(trim(implode('', $data["GROUPCD"])))) {
                for ($g = 0; $g < get_count($data["GROUPCD"]); $g++) {
                    if (0 == $db->getOne(knjx091Query::getCourseGroupcdCnt($data["YEAR"], $data["GRADE"], $data["GROUPCD"][$g]))) {
                        $returnFlg = $this->errSet($masterErrFlg, $masterErrMsg, "※マスタチェック:", $masterSep, $checkdata["GROUPCD"][1]."(".($g+1).")");
                    }
                }
            }
        } elseif ($line == 3) {
            //必須項目チェック
            if (!strlen(trim(implode('', $data["SUBCLASSCD"])))) {
                $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $checkdata["SUBCLASSCD"][1]);
            }

            //SUBCLASS_COMP_SELECT_DATの存在チェック
            if (strlen(trim($data["YEAR"])) && strlen(trim($data["GRADE"])) && strlen(trim(implode('', $data["GROUPCD"]))) && strlen(trim(implode('', $data["SUBCLASSCD"])))) {
                for ($g = 0; $g < get_count($data["SUBCLASSCD"]); $g++) {
                    if (0 == $db->getOne(knjx091Query::getSubclasscdCnt($this, $data["YEAR"], $data["GRADE"], $data["GROUPCD"][$g], $data["SUBCLASSCD"][$g]))) {
                        $returnFlg = $this->errSet($masterErrFlg, $masterErrMsg, "※マスタチェック:", $masterSep, $checkdata["SUBCLASSCD"][1]."(".($g+1).")");
                    }
                }
            }
        }

        if (!$returnFlg) {
            $checkError = $hissuErrMsg.$numericErrMsg.$lengthErrMsg.$orderErrMsg.$masterErrMsg;
            $checkError = $this->checkErrLength($checkError);
            knjx091Query::insertQueryErr($db, $line, $checkError);
        }
        return $returnFlg;
    }

    //エラー出力（CSV出力：明細）
    public function errCheck2_2($db, $data, $checkdata, $line, $subclasscd, $duplicateCsv1, $primaryKey1)
    {
        $returnFlg          = true;
        $hissuErrFlg        = false;    //必須エラーフラグ
        $masterErrFlg       = false;    //マスタエラーフラグ
        $numericErrFlg      = false;    //数字エラーフラグ
        $lengthErrFlg       = false;    //レングスエラーフラグ
        $duplicateCsvErrFlg = false;    //重複(CSV)エラーフラグ
        $valuesErrFlg       = false;
        $orderErrFlg        = false;

        $hissuErrMsg        = "";       //必須エラーメッセージ
        $masterErrMsg       = "";       //マスタエラーメッセージ
        $numericErrMsg      = "";       //数字エラーメッセージ
        $lengthErrMsg       = "";       //レングスエラーメッセージ
        $duplicateCsvErrMsg = "";       //重複(CSV)メッセージ
        $valuesErrMsg       = "";
        $orderErrMsg        = "";

        $hissuSep        = "";
        $masterSep       = "";
        $numericSep      = "";
        $lengthSep       = "";
        $duplicateCsvSep = "";
        $valuesSep       = "";
        $orderSep        = "";

        list($year, $grade) = explode('-', $this->field["YEAR_GRADE"]);

        //重複(CSV)チェック
        $dupFlg1 = $this->duplicateCsvCheck($duplicateCsv1, $primaryKey1, $data);
        if ($dupFlg1[0] == 1) {
            $returnFlg = $this->errSet($duplicateCsvErrFlg, $duplicateCsvErrMsg, "※重複(CSV):", $duplicateCsvSep, "重複1(". $dupFlg1[1] ."行目)");
        }

        foreach ($checkdata as $key => $val) {
            //必須項目チェック
            if (in_array($key, array("YEAR","SEMESTER","COURSECD","MAJORCD","COURSECODE","GRADE","SCHREGNO"))) {
                if (!strlen(trim($data[$key]))) {
                    $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[1]);
                }
            }

            //数値チェック
            if (in_array($key, array("YEAR","SEMESTER","COURSECD","MAJORCD","COURSECODE","SCHREGNO"))) {
                if ($data[$key] != "" && !is_numeric($data[$key])) {
                    $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, $val[1]);
                }
            }

            if ($this->field["SHORI_MEI"] == "1") {
                //文字数チェック
                if (strlen($data[$key]) > $val[0]) {
                    $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[1]);
                }
            }

            //項目チェック
            if ($key == "YEAR" && $data["YEAR"] != $year) {
                $returnFlg = $this->errSet($valuesErrFlg, $valuesErrMsg, "※値不正:", $valuesSep, "履修年度と異なる");
            }
            if ($key == "GRADE" && $data["GRADE"] != $grade) {
                $returnFlg = $this->errSet($valuesErrFlg, $valuesErrMsg, "※値不正:", $valuesSep, "履修学年と異なる");
            }
        }

        //YEAR、SEMESTER、SCHREGNO の入力があった場合
        //SCHREG_REGD_DAT にその生徒が登録されているのかチェックする
        if (strlen(trim($data["YEAR"])) && strlen(trim($data["SEMESTER"])) && strlen(trim($data["SCHREGNO"]))) {
            if (0 == $db->getOne(knjx091Query::schregNoCntSql($data["YEAR"], $data["SEMESTER"], $data["SCHREGNO"], $data["COURSECD"], $data["MAJORCD"], $data["COURSECODE"]))) {
                $returnFlg = $this->errSet($masterErrFlg, $masterErrMsg, "※マスタチェック:", $masterSep, "学籍番号");
            }
        }

        //履修科目の重複チェック
        if ($this->field["SHORI_MEI"] == "1") {
            //履修対象科目を格納
            $flg_subclass = array();
            for ($g = 0; $g < get_count($subclasscd); $g++) {
                if ($data["FLG"][$g] == "1") {
                    $flg_subclass[] = $subclasscd[$g];
                }
            }
            //各件数取得
            $subcl_cnt = $data_cnt = 0;
            foreach (array_count_values($flg_subclass) as $key => $val) {
                $data_cnt += $val;
                $subcl_cnt++;
            }
            //重複チェック
            if ($subcl_cnt != $data_cnt) {
                $returnFlg = $this->errSet($orderErrFlg, $orderErrMsg, "※固有エラー:", $orderSep, "履修科目が重複している");
            }
        }

        if (!$returnFlg) {
            $checkError = $duplicateCsvErrMsg.$hissuErrMsg.$numericErrMsg.$lengthErrMsg.$valuesErrMsg.$orderErrMsg.$masterErrMsg;
            $checkError = $this->checkErrLength($checkError);
            knjx091Query::insertQueryErr($db, $line, $checkError);
        }
        return $returnFlg;
    }


    /**************/
    /* ＣＳＶ出力 */
    /**************/
    public function getDownloadModel()
    {
        //項目
        $header_err = array("MSGROW"        => "行番号",
                            "MSGREMARK"     => "メッセージ");

        //1科目／1行
        if ($this->field["FORM"] == '1') {
            if ($this->Properties["useCurriculumcd"] == '1') {
                $header     = array("YEAR"             => "※年度",
                                    "SEMESTER"         => "※学期",
                                    "COURSECD"         => "※課程",
                                    "COURSENAME"       => "課程名",
                                    "MAJORCD"          => "※学科",
                                    "MAJORNAME"        => "学科名",
                                    "COURSECODE"       => "※コース",
                                    "GRADE"            => "※学年",
                                    "HR_CLASS"         => "組",
                                    "HR_NAME"          => "組名称",
                                    "ATTENDNO"         => "出席番号",
                                    "SCHREGNO"         => "※学籍番号",
                                    "NAME"             => "氏名",
                                    "CLASSCD"          => "※教科コード",
                                    "CLASSNAME"        => "教科名",
                                    "SCHOOL_KIND"      => "※学校種別",
                                    "CURRICULUM_CD"    => "※教育課程コード",
                                    "SUBCLASSCD"       => "※科目コード",
                                    "SUBCLASSNAME"     => "科目名",
                                    "GROUPCD"          => "※選択グループコード",
                                    $this->lastColumn  => $this->lastColumn);

                $db = Query::dbCheckOut();
                //学校種別ヘッダ例取得
                $school_kind_rei = "";
                $result = $db->query(knjx091Query::getSchoolKind());
                while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    if ($school_kind_rei != "") {
                        $school_kind_rei .= " ";
                    }
                    $school_kind_rei .= $row["NAME1"].":".$row["ABBV1"];
                }
                $result->free();
                if ($school_kind_rei == "") {
                    $school_kind_rei = "名称マスタ未設定";
                }
                Query::dbCheckIn($db);

                $header_rei = array("YEAR"            => CTRL_YEAR,
                                    "SEMESTER"        => CTRL_SEMESTER,
                                    "COURSECD"        => "9",
                                    "COURSENAME"      => "課程名",
                                    "MAJORCD"         => "999",
                                    "MAJORNAME"       => "学科名",
                                    "COURSECODE"      => "9999",
                                    "GRADE"           => "01",
                                    "HR_CLASS"        => "001",
                                    "HR_NAME"         => "組名称",
                                    "ATTENDNO"        => "001",
                                    "SCHREGNO"        => "12345678",
                                    "NAME"            => "アルプ太郎",
                                    "CLASSCD"         => "99",
                                    "CLASSNAME"       => "教科名",
                                    "SCHOOL_KIND"     => "H",
                                    "CURRICULUM_CD"   => "2",
                                    "SUBCLASSCD"      => "999999",
                                    "SUBCLASSNAME"    => "科目名",
                                    "GROUPCD"         => "001",
                                    $this->lastColumn => $this->lastColumn);
            } else {
                $header     = array("YEAR"            => "※年度",
                                    "SEMESTER"        => "※学期",
                                    "COURSECD"        => "※課程",
                                    "COURSENAME"      => "課程名",
                                    "MAJORCD"         => "※学科",
                                    "MAJORNAME"       => "学科名",
                                    "COURSECODE"      => "※コース",
                                    "GRADE"           => "※学年",
                                    "HR_CLASS"        => "組",
                                    "HR_NAME"         => "組名称",
                                    "ATTENDNO"        => "出席番号",
                                    "SCHREGNO"        => "※学籍番号",
                                    "NAME"            => "氏名",
                                    "CLASSCD"         => "※教科コード",
                                    "CLASSNAME"       => "教科名",
                                    "CURRICULUM_CD"   => "※教育課程コード",
                                    "SUBCLASSCD"      => "※科目コード",
                                    "SUBCLASSNAME"    => "科目名",
                                    "GROUPCD"         => "※選択グループコード",
                                    $this->lastColumn => $this->lastColumn);

                $header_rei = array("YEAR"            => CTRL_YEAR,
                                    "SEMESTER"        => CTRL_SEMESTER,
                                    "COURSECD"        => "9",
                                    "COURSENAME"      => "課程名",
                                    "MAJORCD"         => "999",
                                    "MAJORNAME"       => "学科名",
                                    "COURSECODE"      => "9999",
                                    "GRADE"           => "01",
                                    "HR_CLASS"        => "001",
                                    "HR_NAME"         => "組名称",
                                    "ATTENDNO"        => "001",
                                    "SCHREGNO"        => "12345678",
                                    "NAME"            => "アルプ太郎",
                                    "CLASSCD"         => "99",
                                    "CLASSNAME"       => "教科名",
                                    "CURRICULUM_CD"   => "2",
                                    "SUBCLASSCD"      => "999999",
                                    "SUBCLASSNAME"    => "科目名",
                                    "GROUPCD"         => "001",
                                    $this->lastColumn => $this->lastColumn);
            }

            $contents  = "";        //クリア
            $header_data = false;   //出力フラグ
            if ($this->field["OUTPUT"]=="1") {
                // ヘッダ出力
                $header_data = true;
                $contents .= implode($header, ",")."\n";
                $contents .= implode($header_rei, ",")."\n";
            } elseif ($this->field["OUTPUT"]=="3") {
                // エラー出力
                $tables = array();
                if ($this->field["SHORI_MEI"] == "1") {
                    $tables = array("1" => "SUBCLASS_STD_SELECT_DAT");
                } else {
                    $tables = array("1" => "SUBCLASS_STD_SELECT_DAT", "2" => "SUBCLASS_COMP_SELECT_DAT");
                }
                $dupTable = $this->getDuplicateErrorHeader(PROGRAMID, $tables);
                if (sizeof($dupTable) > 0) {
                    $headerErr = $this->duplicateCsvHeader($dupTable, $header);
                    $contents .= implode($headerErr, ",")."\n";
                }
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
                    $query = $this->selectCsvErrQuery(PROGRAMID);
                } elseif ($this->field["OUTPUT"]=="4") {
                    //データ
                    $query = knjx091Query::selectMainQuery($this);
                }

                //SQL文発行
                $result = $db->query($query);
                while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    if ($this->field["OUTPUT"]=="4") {                //データ
                        $row = str_replace("\r\n", "", str_replace(",", ".", $row));   //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
                    }
                    $contents .= implode($row, ",") ."\n";
                    $header_data = true;
                }
                $result->free();
                Query::dbCheckIn($db);
            }

            //複数科目／1行
        } else {
            $db = Query::dbCheckOut();
            $header1 = $header2 = $header3 = $header4 = array();

            $header     = array("YEAR"             => "※年度",
                                "SEMESTER"         => "※学期",
                                "COURSECD"         => "※課程",
                                "COURSENAME"       => "課程名",
                                "MAJORCD"          => "※学科",
                                "MAJORNAME"        => "学科名",
                                "COURSECODE"       => "※コース",
                                "GRADE"            => "※学年",
                                "HR_CLASS"         => "組",
                                "HR_NAME"          => "組名称",
                                "ATTENDNO"         => "出席番号",
                                "SCHREGNO"         => "※学籍番号",
                                "NAME"             => "氏名");

            list($year, $grade) = explode('-', $this->field["YEAR_GRADE"]);

            $header1 = array('※履修年度', $year, '※履修学年', $grade,"","","","","","","","","");

            foreach ($header as $key => $val) {
                $header2[] = "";
                $header3[] = "";
                $header4[] = $val;
            }

            $Hcnt = 0;
            $HeaderData = array();
            $result = $db->query(knjx091Query::getHeaderData($this));
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $header1[] = $row["GROUPCD"];
                $header2[] = $row["GROUPNAME"];
                $header3[] = $row["SUBCLASSCD"];
                $header4[] = $row["SUBCLASSNAME"];
                $HeaderData["GROUPCD"][] = $row["GROUPCD"];
                $HeaderData["SUBCLASSCD"][] = $row["SUBCLASSCD"];
                $Hcnt++;
            }
            $result->free();

            $header1[] = $this->lastColumn;
            $header2[] = $this->lastColumn;
            $header3[] = $this->lastColumn;
            $header4[] = $this->lastColumn;

            //ヘッダ例
            $header_rei = array("YEAR"          => $year,
                                "SEMESTER"      => CTRL_SEMESTER,
                                "COURSECD"      => "9",
                                "COURSENAME"    => "課程名",
                                "MAJORCD"       => "999",
                                "MAJORNAME"     => "学科名",
                                "COURSECODE"    => "9999",
                                "GRADE"         => $grade,
                                "HR_CLASS"      => "001",
                                "HR_NAME"       => "組名称",
                                "ATTENDNO"      => "001",
                                "SCHREGNO"      => "12345678",
                                "NAME"          => "アルプ太郎");

            for ($i = 0; $i < $Hcnt; $i++) {
                $header_rei[] = ($i == 0) ? 1 : "";
            }
            $header_rei[] = $this->lastColumn;

            $contents  = "";        //クリア
            $header_data = false;   //出力フラグ
            if ($this->field["OUTPUT"] == "1") {
                // ヘッダ出力
                $header_data = true;
                $contents .= implode($header1, ",")."\n";
                $contents .= implode($header2, ",")."\n";
                $contents .= implode($header3, ",")."\n";
                $contents .= implode($header4, ",")."\n";
                $contents .= implode($header_rei, ",")."\n";
            } elseif ($this->field["OUTPUT"] == "3") {
                // エラー出力
                $headerDuplicate = array("YEAR"          => "年度",
                                         "SEMESTER"      => "学期",
                                         "SCHREGNO"      => "学籍番号",
                                         "CLASSCD"       => "教科コード",
                                         "SCHOOL_KIND"   => "学校種別",
                                         "CURRICULUM_CD" => "教育課程コード",
                                         "SUBCLASSCD"    => "科目コード",
                                         "GROUPCD"       => "選択グループコード");
                $tables = array();
                $tables = array("1" => "SUBCLASS_STD_SELECT_DAT");
                $dupTable = $this->getDuplicateErrorHeader(PROGRAMID, $tables);
                if (sizeof($dupTable) > 0) {
                    $headerErr = $this->duplicateCsvHeader($dupTable, $headerDuplicate);
                    $contents .= implode($headerErr, ",")."\n";
                }
                $contents .= implode($header_err, ",")."\n";
            } elseif ($this->field["OUTPUT"] == "4") {
                // データ出力
                $contents .= implode($header1, ",")."\n";
                $contents .= implode($header2, ",")."\n";
                $contents .= implode($header3, ",")."\n";
                $contents .= implode($header4, ",")."\n";
            }

            //ヘッダ以外
            if ($this->field["OUTPUT"] != "1") {
                if ($this->field["OUTPUT"] == "3") {
                    //エラー
                    $query = $this->selectCsvErrQuery(PROGRAMID);
                    $result = $db->query($query);
                    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                        $contents .= implode($row, ",") ."\n";
                        $header_data = true;
                    }
                    $result->free();
                } elseif ($this->field["OUTPUT"] == "4") {
                    //データ
                    $query = knjx091Query::getSchList($this);
                    $result = $db->query($query);
                    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                        $contents .= implode($row, ",");
                        $sep = ",";
                        //SUBCLASS_STD_SELECT_DAT取得
                        for ($i = 0; $i < get_count($HeaderData["SUBCLASSCD"]); $i++) {
                            $cnt = $db->getOne(knjx091Query::getSubclassStdSelectDat($this, $row["SCHREGNO"], $HeaderData["GROUPCD"][$i], $HeaderData["SUBCLASSCD"][$i]));
                            $flg = ($cnt > 0) ? 1 : "";
                            $contents .= $sep.$flg;
                            $header_data = true;
                        }
                        $contents .= ",".$this->lastColumn."\n";
                    }
                    $result->free();
                }
            }
            Query::dbCheckIn($db);
        }

        //ＣＳＶ出力
        if ($header_data) {
            if ($this->field["OUTPUT"] == "3") {    //エラー
                $couse = "エラー出力";
            } else {                                //エラー以外
                $couse = "履修登録データ";
            }
            $filename = sprintf("%s.csv", $couse);
            common::downloadContents($contents, $filename);
            return true;
        } else {
            $this->warning = "該当データは、ありません。";
            return false;
        }
    }

    //講座名簿から自動履修登録
    public function getDownloadModel2()
    {

        // ＣＳＶ項目
        $header     = array("YEAR"            => "※年度",
                            "SEMESTER"        => "※学期",
                            "GROUPCD"         => "※選択グループコード",
                            "CLASSCD"         => "※教科コード",
                            "SCHOOL_KIND"     => "※学校種別",
                            "CURRICULUM_CD"   => "※教育課程コード",
                            "SUBCLASSCD"      => "※科目コード",
                            "GRADE"           => "※学年",
                            "HR_CLASS"        => "組",
                            "ATTENDNO"        => "出席番号",
                            "NAME"            => "氏名",
                            "SCHREGNO"        => "※学籍番号",
                            $this->lastColumn => $this->lastColumn);

        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $header_data = false;
        $contents  = "";
        $contents .= implode($header, ",")."\n";

        //講座名簿から履修登録をする
        $query = knjx091Query::getChairStdDat($this);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $auto_cd = "";
            //科目ごとの600未満のGROUPCDの件数
            $groupcd_cnt = get_count($db->getcol(knjx091Query::getSubclassCompSelectDat($this, $row, "under")));

            //履修選択グループ取得
            if ($groupcd_cnt == 1) {
                $groupcd = $db->getOne(knjx091Query::getSubclassCompSelectDat($this, $row, "under"));
            } else {
                $groupcd = $db->getOne(knjx091Query::getSubclassCompSelectDat($this, $row, "over"));
                $max = $db->getOne(knjx091Query::getSubclassCompSelectMst($row));
                $max = ($max) ? $max+1 : "600";
                if (!$groupcd) {
                    $auto_cd = "1";
                }
                $groupcd = ($groupcd) ? $groupcd : $max;
            }

            //SUBCLASS_STD_SELECT_DAT追加
            $query = knjx091Query::insertAutoSubclassStd($this, $row, $groupcd);
            $db->query($query);

            //600以上のGROUPCDを新規追加の場合
            if ($auto_cd == "1") {
                //SUBCLASS_COMP_SELECT_DAT追加
                $query = knjx091Query::insertAutoSubclassCompSelectDat($this, $row, $groupcd);
                $db->query($query);
                //SUBCLASS_COMP_SELECT_MST追加
                $query = knjx091Query::insertAutoSubclassCompSelectMst($row, $groupcd);
                $db->query($query);
            }

            // CSV出力
            $row = str_replace(",", ".", $row); // データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
            $contents .= $row["YEAR"] .",";
            $contents .= $row["SEMESTER"] .",";
            $contents .= $groupcd .",";
            if ($this->Properties["useCurriculumcd"] == '1') {
                $contents .= $row["CLASSCD"] .",";
                $contents .= $row["SCHOOL_KIND"] .",";
                $contents .= $row["CURRICULUM_CD"] .",";
            }
            $contents .= $row["SUBCLASSCD"] .",";
            $contents .= $row["GRADE"] .",";
            $contents .= $row["HR_CLASS"] .",";
            $contents .= $row["ATTENDNO"] .",";
            $contents .= $row["NAME"] .",";
            $contents .= $row["SCHREGNO"] .",";
            $contents .= $this->lastColumn ."\n";

            $header_data = true;
        }
        $result->free();

        //1科目のみの600未満の履修グループと同一科目の600以上の履修グループの処理
        $query = knjx091Query::getSameGroupcdList($this);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //600以上の履修グループを登録している生徒を取得
            $queryS = knjx091Query::getOver600StdList($this, $row);
            $resultS = $db->query($queryS);
            while ($rowS = $resultS->fetchRow(DB_FETCHMODE_ASSOC)) {
                //SUBCLASS_STD_SELECT_DAT追加
                $query = knjx091Query::insertAutoSubclassStd($this, $rowS, $row["U_GROUPCD"]);
                $db->query($query);
                //SUBCLASS_STD_SELECT_DAT削除
                $query = knjx091Query::deleteOver600StdDat($this, $rowS);
                $db->query($query);
            }
            $resultS->free();

            //SUBCLASS_COMP_SELECT_DAT削除（GROUPCDが600以上 && 履修生徒なし）
            $query = knjx091Query::deleteOver600SelectDat($this, $row);
            $db->query($query);
            //SUBCLASS_COMP_SELECT_MST削除（GROUPCDが600以上 && SUBCLASS_COMP_SELECT_DATなし）
            $query = knjx091Query::deleteOver600SelectMst($this, $row);
            $db->query($query);
        }
        $result->free();

        $db->commit();
        Query::dbCheckIn($db);

        // ＣＳＶ出力
        if ($header_data) {
            if (!$this->Properties["useXLS"]) {
                $filename = sprintf("%s.csv", "履修科目登録データ(自動生成)");
                common::downloadContents($contents, $filename);
                return true;
            } else {
                $this->warning = "データを更新しました。";
                return false;
            }
        } else {
            $this->warning = "該当データは、ありません。";
            return false;
        }
    }
}
