<?php

require_once('for_php7.php');
class knjx093cModel extends Model
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

        $this->field = array("OUTPUT"        =>  VARS::post("OUTPUT"),        //出力取込種別
                             "YEAR_SEMESTER" =>  VARS::post("YEAR_SEMESTER"), //年度_学期
                             "RIREKI_CODE"   =>  VARS::post("RIREKI_CODE"),   //履修登録日コード
                             "GRADE"         =>  VARS::post("GRADE"),         //学年
                             "SUBCLASS"      =>  VARS::post("SUBCLASS"),      //科目
                             "HEADER"        =>  VARS::post("HEADER"),        //ヘッダ有
                             "SHORI_MEI"     =>  VARS::post("SHORI_MEI")      //処理名
                             );

        $this->cmd = VARS::request("cmd");

        if (VARS::request("cmd") != "") {
            $this->field["YEAR"]     = substr(VARS::post("YEAR_SEMESTER"), 0, 4);
            $this->field["SEMESTER"] = substr(VARS::post("YEAR_SEMESTER"), 4);
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
        //ファイルの文字列の取得
        $this->data_arr = array();
        $this->data_arr = $this->getFileString();

        //データの整合性チェック
        if ($this->isDataMatched($this->data_arr)) {
            $errFlg = $this->checkCsvErrQuery(PROGRAMID);
            if ($errFlg) {
                if ($this->field["SHORI_MEI"] == "1") {
                    knjx093cQuery::deleteQueryCsv($this, $this->data_arr); //処理件数を返すけれども使わない。とりあえず代入しただけ。
                    knjx093cQuery::insertQueryCsv($this, $this->data_arr);
                } else {
                    knjx093cQuery::deleteQueryCsv($this, $this->data_arr);
                }

                $this->setMessage("MSG202", "更新件数".get_count($this->data_arr)."件");
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
                                   "RIREKI_CODE"   => array(2,"履修登録日コード"),
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
                                   "RIREKI_CODE"   => array(2,"履修登録日コード"),
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
            $primaryKey1 = array();
            $duplicateCsv1 = array();
            $primaryKey1 = $this->getPrimaryKey('SUBCLASS_STD_SELECT_RIREKI_DAT');
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
                                      "RIREKI_CODE"    => (strlen(trim($tmp[2])))  ? sprintf("%02d", $tmp[2])  : "",
                                      "GROUPCD"        => (strlen(trim($tmp[3])))  ? sprintf("%03d", $tmp[3])  : "",
                                      "CLASSCD"        => (strlen(trim($tmp[4])))  ? sprintf("%02d", $tmp[4])  : "",
                                      "SCHOOL_KIND"    => (strlen(trim($tmp[5])))  ? $tmp[5]  : "",
                                      "CURRICULUM_CD"  => (strlen(trim($tmp[6])))  ? sprintf("%01d", $tmp[6])  : "",
                                      "SUBCLASSCD"     => (strlen(trim($tmp[7])))  ? sprintf("%06d", $tmp[7])  : "",
                                      "GRADE"          => (strlen(trim($tmp[9])))  ? sprintf("%02d", $tmp[9])  : "",
                                      "SCHREGNO"       => (strlen(trim($tmp[13]))) ? sprintf("%08d", $tmp[13]) : "");
                    } else {
                        $data = array("YEAR"           => (strlen(trim($tmp[0])))  ? sprintf("%04d", $tmp[0])  : "",
                                      "SEMESTER"       => (strlen(trim($tmp[1])))  ? sprintf("%01d", $tmp[1])  : "",
                                      "RIREKI_CODE"    => (strlen(trim($tmp[2])))  ? sprintf("%02d", $tmp[2])  : "",
                                      "GROUPCD"        => (strlen(trim($tmp[3])))  ? sprintf("%03d", $tmp[3])  : "",
                                      "CLASSCD"        => (strlen(trim($tmp[4])))  ? sprintf("%02d", $tmp[4])  : "",
                                      "CURRICULUM_CD"  => (strlen(trim($tmp[5])))  ? sprintf("%01d", $tmp[5])  : "",
                                      "SUBCLASSCD"     => (strlen(trim($tmp[6])))  ? sprintf("%06d", $tmp[6])  : "",
                                      "GRADE"          => (strlen(trim($tmp[8])))  ? sprintf("%02d", $tmp[8])  : "",
                                      "SCHREGNO"       => (strlen(trim($tmp[12]))) ? sprintf("%08d", $tmp[12]) : "");
                    }

                    //エラーチェック
                    if ($this->errCheck($db, $data, $checkdata, $line, $duplicateCsv1, $primaryKey1)) {
                        $temp_arr[$i] = $data;
                        $i++;
                    }
                    foreach ($primaryKey1 as $key => $val) {
                        $duplicateCsv1[$line][$val] = $data[$val];
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
    public function errCheck($db, $data, $checkdata, $line, $duplicateCsv1, $primaryKey1)
    {
        $returnFlg          = true;
        $hissuErrFlg        = false;    //必須エラーフラグ
        $masterErrFlg       = false;    //マスタエラーフラグ
        $numericErrFlg      = false;    //数字エラーフラグ
        $lengthErrFlg       = false;    //レングスエラーフラグ
        $duplicateCsvErrFlg = false;    //重複(CSV)

        $hissuErrMsg        = "";       //必須エラーメッセージ
        $masterErrMsg       = "";       //マスタエラーメッセージ
        $numericErrMsg      = "";       //数字エラーメッセージ
        $lengthErrMsg       = "";       //レングスエラーメッセージ
        $duplicateCsvErrMsg = "";       //重複(CSV)

        $hissuSep        = "";
        $masterSep       = "";
        $numericSep      = "";
        $lengthSep       = "";
        $duplicateCsvSep = "";

        $dupFlg = $this->duplicateCsvCheck($duplicateCsv1, $primaryKey1, $data);
        if ($dupFlg[0] == 1) {
            $returnFlg = $this->errSet($duplicateCsvErrFlg, $duplicateCsvErrMsg, "※重複(CSV):", $duplicateCsvSep, "重複1(". $dupFlg[1] ."行目)");
        }
        //学校種別
        $school_kind = $db->getCol(knjx093cQuery::getSchoolKind());
        //教育課程
        $curriculum_cd = $db->getCol(knjx093cQuery::getCurriculumCd());

        foreach ($checkdata as $key => $val) {
            if ($this->Properties["useCurriculumcd"] == '1') {
                //必須項目チェック
                if (in_array($key, array("YEAR","SEMESTER","RIREKI_CODE","GROUPCD","CLASSCD","SCHOOL_KIND","CURRICULUM_CD","SUBCLASSCD","GRADE","SCHREGNO"))) {
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
                if (in_array($key, array("YEAR","SEMESTER","RIREKI_CODE","GROUPCD","CLASSCD","CURRICULUM_CD","SUBCLASSCD","GRADE","SCHREGNO"))) {
                    if (!strlen(trim($data[$key]))) {
                        $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[1]);
                    } else {
                        if ($this->field["SHORI_MEI"] == "1") {
                            if ($key == 'CLASSCD' && 0 == $db->getOne(knjx093cQuery::classcdCntSql($this, $data, $data[$key]))) {
                                $returnFlg = $this->errSet($masterErrFlg, $masterErrMsg, "※マスタチェック:", $masterSep, $val[1]);
                            }
                            if ($key == 'SUBCLASSCD' && 0 == $db->getOne(knjx093cQuery::subclassCntSql($this, $data, $data[$key]))) {
                                $returnFlg = $this->errSet($masterErrFlg, $masterErrMsg, "※マスタチェック:", $masterSep, $val[1]);
                            }
                        }
                    }
                }
            }

            //数値チェック
            if (in_array($key, array("YEAR","SEMESTER","RIREKI_CODE","GROUPCD","CLASSCD","CURRICULUM_CD","SUBCLASSCD","SCHREGNO"))) {
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
                if (0 == $db->getOne(knjx093cQuery::classcdCntSql($this, $data, $data["CLASSCD"]))) {
                    $returnFlg = $this->errSet($masterErrFlg, $masterErrMsg, "※マスタチェック:", $masterSep, "教科コード");
                }
                if (0 == $db->getOne(knjx093cQuery::subclassCntSql($this, $data, $data["SUBCLASSCD"]))) {
                    $returnFlg = $this->errSet($masterErrFlg, $masterErrMsg, "※マスタチェック:", $masterSep, "科目コード");
                }
            }
        }

        //YEAR、SEMESTER、SCHREGNO の入力があった場合
        //SCHREG_REGD_DAT にその生徒が登録されているのかチェックする
        if (strlen(trim($data["YEAR"])) && strlen(trim($data["SEMESTER"])) && strlen(trim($data["SCHREGNO"]))) {
            if (0 == $db->getOne(knjx093cQuery::schregNoCntSql($data["YEAR"], $data["SEMESTER"], $data["SCHREGNO"]))) {
                $returnFlg = $this->errSet($masterErrFlg, $masterErrMsg, "※マスタチェック:", $masterSep, "学籍番号");
            }
        }

        //SUBCLASS_COMP_SELECT_DATの存在チェック
        if ($this->Properties["useCurriculumcd"] == '1') {
            if (strlen(trim($data["YEAR"])) && strlen(trim($data["GRADE"])) && strlen(trim($data["GROUPCD"])) && strlen(trim($data["CLASSCD"])) && strlen(trim($data["SUBCLASSCD"])) && strlen(trim($data["SCHOOL_KIND"]))) {
                if (0 == $db->getOne(knjx093cQuery::groupcdCntSql($data["YEAR"], $data["GRADE"], $data["GROUPCD"], $data["CLASSCD"], $data["SUBCLASSCD"], $data["SCHOOL_KIND"]))) {
                    $returnFlg = $this->errSet($masterErrFlg, $masterErrMsg, "※マスタチェック:", $masterSep, "選択グループコード");
                }
            }
        } else {
            if (strlen(trim($data["YEAR"])) && strlen(trim($data["GRADE"])) && strlen(trim($data["GROUPCD"])) && strlen(trim($data["CLASSCD"])) && strlen(trim($data["SUBCLASSCD"]))) {
                if (0 == $db->getOne(knjx093cQuery::groupcdCntSql($data["YEAR"], $data["GRADE"], $data["GROUPCD"], $data["CLASSCD"], $data["SUBCLASSCD"]))) {
                    $returnFlg = $this->errSet($masterErrFlg, $masterErrMsg, "※マスタチェック:", $masterSep, "選択グループコード");
                }
            }
        }

        if (0 == $db->getOne(knjx093cQuery::getRirekiCode($data["YEAR"], $data["RIREKI_CODE"]))) {
            $returnFlg = $this->errSet($masterErrFlg, $masterErrMsg, "※マスタチェック:", $masterSep, "履修登録日コード");
        }

        if (!$returnFlg) {
            $checkError = $duplicateCsvErrMsg.$hissuErrMsg.$numericErrMsg.$lengthErrMsg.$masterErrMsg;
            $checkError = $this->checkErrLength($checkError);
            knjx093cQuery::insertQueryErr($db, $line, $checkError);
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

    /**************/
    /* ＣＳＶ出力 */
    /**************/
    public function getDownloadModel()
    {
        //項目
        $header_err = array("MSGROW"        => "行番号",
                            "MSGREMARK"     => "メッセージ");

        if ($this->Properties["useCurriculumcd"] == '1') {
            $header     = array("YEAR"            => "※年度",
                                "SEMESTER"        => "※学期",
                                "RIREKI_CODE"     => "※履修登録日コード",
                                "GROUPCD"         => "※選択グループコード",
                                "CLASSCD"         => "※教科コード",
                                "SCHOOL_KIND"     => "※学校種別",
                                "CURRICULUM_CD"   => "※教育課程コード",
                                "SUBCLASSCD"      => "※科目コード",
                                "SUBCLASSNAME"    => "科目名",
                                "GRADE"           => "※学年",
                                "HR_CLASS"        => "組",
                                "ATTENDNO"        => "出席番号",
                                "NAME"            => "氏名",
                                "SCHREGNO"        => "※学籍番号",
                                $this->lastColumn =>  $this->lastColumn);

            $db = Query::dbCheckOut();
            //学校種別ヘッダ例取得
            $school_kind_rei = "";
            $result = $db->query(knjx093cQuery::getSchoolKind());
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
                                "RIREKI_CODE"     => "01",
                                "GROUPCD"         => "001",
                                "CLASSCD"         => "99",
                                "SCHOOL_KIND"     => $school_kind_rei,
                                "CURRICULUM_CD"   => "2",
                                "SUBCLASSCD"      => "999999",
                                "SUBCLASSNAME"    => "国語",
                                "GRADE"           => "01",
                                "HR_CLASS"        => "001",
                                "ATTENDNO"        => "001",
                                "NAME"            => "アルプ太郎",
                                "SCHREGNO"        => "12345678",
                                $this->lastColumn =>  $this->lastColumn);
        } else {
            $header     = array("YEAR"            => "※年度",
                                "SEMESTER"        => "※学期",
                                "RIREKI_CODE"      => "※履修登録日コード",
                                "GROUPCD"         => "※選択グループコード",
                                "CLASSCD"         => "※教科コード",
                                "CURRICULUM_CD"   => "※教育課程コード",
                                "SUBCLASSCD"      => "※科目コード",
                                "SUBCLASSNAME"    => "科目名",
                                "GRADE"           => "※学年",
                                "HR_CLASS"        => "組",
                                "ATTENDNO"        => "出席番号",
                                "NAME"            => "氏名",
                                "SCHREGNO"        => "※学籍番号",
                                $this->lastColumn =>  $this->lastColumn);

            $header_rei = array("YEAR"            => CTRL_YEAR,
                                "SEMESTER"        => CTRL_SEMESTER,
                                "RIREKI_CODE"     => "01",
                                "GROUPCD"         => "001",
                                "CLASSCD"         => "99",
                                "CURRICULUM_CD"   => "2",
                                "SUBCLASSCD"      => "999999",
                                "SUBCLASSNAME"    => "国語",
                                "GRADE"           => "01",
                                "HR_CLASS"        => "001",
                                "ATTENDNO"        => "001",
                                "NAME"            => "アルプ太郎",
                                "SCHREGNO"        => "12345678",
                                $this->lastColumn =>  $this->lastColumn);
        }

        $contents  = "";    //クリア

        $header_data = false;   //出力フラグ


        if ($this->field["OUTPUT"]=="1") {
            // ヘッダ出力
            $header_data = true;
            $contents .= implode($header, ",")."\n";
            $contents .= implode($header_rei, ",")."\n";
        } elseif ($this->field["OUTPUT"]=="3") {
            // エラー出力
            $headerDuplicate = array();
            $headerDuplicate["YEAR"]          = "年度";
            $headerDuplicate["SEMESTER"]      = "学期";
            $headerDuplicate["RIREKI_CODE"]   = "履修登録日コード";
            $headerDuplicate["GROUPCD"]       = "選択グループコード";
            $headerDuplicate["CLASSCD"]       = "教科コード";
            $headerDuplicate["SCHOOL_KIND"]   = "学校種別";
            $headerDuplicate["CURRICULUM_CD"] = "教育課程コード";
            $headerDuplicate["SUBCLASSCD"]    = "科目コード";
            $headerDuplicate["SCHREGNO"]      = "学籍番号";
            $tables = array();
            $tables = array("1" => "SUBCLASS_STD_SELECT_RIREKI_DAT");
            $dupTable = $this->getDuplicateErrorHeader(PROGRAMID, $tables);
            if (sizeof($dupTable) > 0) {
                $headerErr = $this->duplicateCsvHeader($dupTable, $headerDuplicate);
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
                $query = knjx093cQuery::selectMainQuery($this);
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

        //ＣＳＶ出力
        if ($header_data) {
            if ($this->field["OUTPUT"]=="3") {        //エラー
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
}
