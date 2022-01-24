<?php

require_once('for_php7.php');
class knjx091bModel extends Model
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
                             "YEAR_GRADE"       =>  VARS::post("YEAR_GRADE"),       //年度・学年
                             "YEAR_SEMESTER"    =>  VARS::post("YEAR_SEMESTER"),    //年度_学期
                             "GRADE"            =>  VARS::post("GRADE"),            //学年
                             "SUBCLASS"         =>  VARS::post("SUBCLASS"),         //科目
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
        //ファイルの文字列の取得
        $this->data_arrH = $this->data_arr = array();
        list($this->data_arrH, $this->data_arr) = $this->getFileString();

        //データの整合性チェック
        if ($this->isDataMatched($this->data_arr)) {
            $errFlg = $this->checkCsvErrQuery(PROGRAMID);
            if ($errFlg) {
                if ($this->field["SHORI_MEI"] == "1") {
                    knjx091bQuery::deleteQueryCsv($this, $this->data_arrH, $this->data_arr); //処理件数を返すけれども使わない。とりあえず代入しただけ。
                    knjx091bQuery::insertQueryCsv($this, $this->data_arrH, $this->data_arr);
                } else {
                    knjx091bQuery::deleteQueryCsv($this, $this->data_arrH, $this->data_arr);
                }

                $this->setMessage("MSG202", "更新件数".get_count($this->data_arr)."件");
            }
        }
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

    /***********************/
    /* CSVファイルを配列へ */
    /***********************/
    public function getFileString()
    {
        $temp_arrH = $temp_arr = array();
        //HTTP POSTによりアップロードされたファイルかどうかを調べる
        if (!is_uploaded_file($this->field["FILE"]["tmp_name"])) {
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
            $primaryKey1 = array();
            $duplicateCsv1 = array();
            $primaryKey1 = $this->getPrimaryKey('SUBCLASS_STD_SELECT_DAT');
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
                        for ($j = 10; $j < get_count($tmp); $j++) {
                            if ($tmp[$j] == $this->lastColumn) {
                                continue;
                            } else {
                                $dataH["GROUPCD"][] = (strlen(trim($tmp[$j]))) ? $tmp[$j] : "";
                            }
                        }
                        //エラーチェック
                        if ($this->errCheck1($db, $dataH, $checkdata1, $line)) {
                            $temp_arrH = $dataH;
                        }
                    } elseif ($line == 2 || $line == 4) {
                        $line++;
                        continue;
                    } elseif ($line == 3) {
                        //科目コード格納
                        for ($j = 10; $j < get_count($tmp); $j++) {
                            if ($tmp[$j] == $this->lastColumn) {
                                continue;
                            } else {
                                $dataH["SUBCLASSCD"][] = (strlen(trim($tmp[$j]))) ? $tmp[$j] : "";
                            }
                        }
                        //エラーチェック
                        if ($this->errCheck1($db, $dataH, $checkdata1, $line)) {
                            $temp_arrH = $dataH;
                        }
                    } else {
                        $data = array("YEAR"           => (strlen(trim($tmp[0])))  ? sprintf("%04d", $tmp[0])  : "",
                                      "SEMESTER"       => (strlen(trim($tmp[1])))  ? sprintf("%01d", $tmp[1])  : "",
                                      "COURSECD"       => (strlen(trim($tmp[2])))  ? sprintf("%01d", $tmp[2])  : "",
                                      "MAJORCD"        => (strlen(trim($tmp[3])))  ? sprintf("%03d", $tmp[3])  : "",
                                      "COURSECODE"     => (strlen(trim($tmp[4])))  ? sprintf("%04d", $tmp[4])  : "",
                                      "GRADE"          => (strlen(trim($tmp[5])))  ? sprintf("%02d", $tmp[5])  : "",
                                      "SCHREGNO"       => (strlen(trim($tmp[8])))  ? sprintf("%08d", $tmp[8]) : "");
                        //チェック格納
                        for ($j = 10; $j < get_count($tmp); $j++) {
                            if ($tmp[$j] == $this->lastColumn) {
                                continue;
                            } else {
                                $data["FLG"][] = (strlen(trim($tmp[$j]))) ? $tmp[$j] : "";
                            }
                        }
                        //エラーチェック
                        if ($this->errCheck2($db, $data, $checkdata2, $line, $dataH["SUBCLASSCD"], $duplicateCsv1, $primaryKey1)) {
                            $temp_arr[$i] = $data;
                            $i++;
                        }
                    }
                    foreach ($primaryKey1 as $key => $val) {
                        $duplicateCsv1[$line][$val] = $data[$val];
                    }
                    $line++;
                }
            }

            $db->commit();  // トランザクションをコミットする。
            Query::dbCheckIn($db);
            fclose($fp);
        }

        return array($temp_arrH, $temp_arr);
    }

    /******************/
    /* エラーチェック */
    /******************/
    //エラー表示（ポップアップ表示：ヘッダ）
    public function errCheck1($db, $data, $checkdata, $line)
    {
        $returnFlg     = true;
        $hissuErrFlg   = false;    //必須エラーフラグ
        $masterErrFlg  = false;    //マスタエラーフラグ
        $numericErrFlg = false;    //数字エラーフラグ
        $lengthErrFlg  = false;    //レングスエラーフラグ
        $orderErrFlg   = false;

        $hissuErrMsg   = "";       //必須エラーメッセージ
        $masterErrMsg  = "";       //マスタエラーメッセージ
        $numericErrMsg = "";       //数字エラーメッセージ
        $lengthErrMsg  = "";       //レングスエラーメッセージ
        $orderErrMsg   = "";

        $hissuSep   = "";
        $masterSep  = "";
        $numericSep = "";
        $lengthSep  = "";
        $orderSep   = "";
        if ($line == 1) {
            foreach ($checkdata as $key => $val) {
                //必須項目チェック
                if (in_array($key, array("YEAR","GRADE"))) {
                    if (!strlen(trim($data[$key]))) {
                        $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[1]);
                    }
                }
                if ($key == "GROUPCD") {
                    if ($data[$key] && !strlen(trim(implode('', $data[$key])))) {
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
            if (strlen(trim($data["YEAR"])) && strlen(trim($data["GRADE"])) && $data["GROUPCD"] && strlen(trim(implode('', $data["GROUPCD"])))) {
                for ($g = 0; $g < get_count($data["GROUPCD"]); $g++) {
                    if (0 == $db->getOne(knjx091bQuery::getCourseGroupcdCnt($data["YEAR"], $data["GRADE"], $data["GROUPCD"][$g]))) {
                        $returnFlg = $this->errSet($masterErrFlg, $masterErrMsg, "※マスタチェック:", $masterSep, $checkdata["GROUPCD"][1]."(".($g+1).")");
                    }
                }
            }
        } elseif ($line == 3) {
            //必須項目チェック
            if ($data["SUBCLASSCD"] && !strlen(trim(implode('', $data["SUBCLASSCD"])))) {
                $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $checkdata["SUBCLASSCD"][1]);
            }

            //SUBCLASS_COMP_SELECT_DATの存在チェック
            if (strlen(trim($data["YEAR"])) && strlen(trim($data["GRADE"])) && $data["GROUPCD"] && strlen(trim(implode('', $data["GROUPCD"]))) && $data["SUBCLASSCD"] && strlen(trim(implode('', $data["SUBCLASSCD"])))) {
                for ($g = 0; $g < get_count($data["SUBCLASSCD"]); $g++) {
                    if (0 == $db->getOne(knjx091bQuery::getSubclasscdCnt($this, $data["YEAR"], $data["GRADE"], $data["GROUPCD"][$g], $data["SUBCLASSCD"][$g]))) {
                        $returnFlg = $this->errSet($masterErrFlg, $masterErrMsg, "※マスタチェック:", $masterSep, $checkdata["SUBCLASSCD"][1]."(".($g+1).")");
                    }
                }
            }
        }

        if (!$returnFlg) {
            $checkError = $hissuErrMsg.$numericErrMsg.$lengthErrMsg.$masterErrMsg.$orderErrMsg;
            $checkError = $this->checkErrLength($checkError);
            knjx091bQuery::insertQueryErr($db, $line, $checkError);
        }
        return $returnFlg;
    }

    //エラー出力（CSV出力：明細）
    public function errCheck2($db, $data, $checkdata, $line, $subclasscd, $duplicateCsv1, $primaryKey1)
    {
        $returnFlg          = true;
        $hissuErrFlg        = false;    //必須エラーフラグ
        $masterErrFlg       = false;    //マスタエラーフラグ
        $numericErrFlg      = false;    //数字エラーフラグ
        $lengthErrFlg       = false;    //レングスエラーフラグ
        $duplicateCsvErrFlg = false;    //重複(CSV)
        $orderErrFlg        = false;
        $valuesErrFlg       = false;

        $hissuErrMsg        = "";       //必須エラーメッセージ
        $masterErrMsg       = "";       //マスタエラーメッセージ
        $numericErrMsg      = "";       //数字エラーメッセージ
        $lengthErrMsg       = "";       //レングスエラーメッセージ
        $duplicateCsvErrMsg = "";       //重複(CSV)
        $orderErrMsg        = "";
        $valuesErrMsg       = "";

        $hissuSep        = "";
        $masterSep       = "";
        $numericSep      = "";
        $lengthSep       = "";
        $duplicateCsvSep = "";
        $orderSep        = "";
        $valuesSep       = "";

        list($year, $grade) = explode('-', $this->field["YEAR_GRADE"]);

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
            if (0 == $db->getOne(knjx091bQuery::schregNoCntSql($data["YEAR"], $data["SEMESTER"], $data["SCHREGNO"], $data["COURSECD"], $data["MAJORCD"], $data["COURSECODE"]))) {
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
            $checkError = $duplicateCsvErrMsg.$hissuErrMsg.$numericErrMsg.$lengthErrMsg.$orderErrMsg.$masterErrMsg.$valuesErrMsg;
            $checkError = $this->checkErrLength($checkError);
            knjx091bQuery::insertQueryErr($db, $line, $checkError);
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

        $db = Query::dbCheckOut();
        $header1 = $header2 = $header3 = $header4 = array();

        $header = array("YEAR"          => "※年度",
                        "SEMESTER"      => "※学期",
                        "COURSECD"      => "※課程",
                        "MAJORCD"       => "※学科",
                        "COURSECODE"    => "※コース",
                        "GRADE"         => "※学年",
                        "HR_CLASS"      => "組",
                        "ATTENDNO"      => "出席番号",
                        "SCHREGNO"      => "※学籍番号",
                        "NAME"          => "氏名");

        list($year, $grade) = explode('-', $this->field["YEAR_GRADE"]);

        $header1 = array('※履修年度', $year, '※履修学年', $grade,"","","","","","");

        foreach ($header as $key => $val) {
            $header2[] = "";
            $header3[] = "";
            $header4[] = $val;
        }

        $Hcnt = 0;
        $HeaderData = array();
        $result = $db->query(knjx091bQuery::getHeaderData($this));
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
                            "MAJORCD"       => "999",
                            "COURSECODE"    => "9999",
                            "GRADE"         => $grade,
                            "HR_CLASS"      => "001",
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
            $headerDuplicate = array("YEAR"          => "※年度",
                                     "SEMESTER"      => "※学期",
                                     "GROUPCD"       => "選択グループコード",
                                     "CLASSCD"       => "教科コード",
                                     "SCHOOL_KIND"   => "学校校種",
                                     "CURRICULUM_CD" => "教育課程コード",
                                     "SUBCLASSCD"    => "科目コード",
                                     "SCHREGNO"      => "※学籍番号");
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
                $query = knjx091bQuery::getSchList($this);
                $result = $db->query($query);
                while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    $contents .= implode($row, ",");
                    $sep = ",";
                    //SUBCLASS_STD_SELECT_DAT取得
                    for ($i = 0; $i < get_count($HeaderData["SUBCLASSCD"]); $i++) {
                        $cnt = $db->getOne(knjx091bQuery::getSubclassStdSelectDat($this, $row["SCHREGNO"], $HeaderData["GROUPCD"][$i], $HeaderData["SUBCLASSCD"][$i]));
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
}
