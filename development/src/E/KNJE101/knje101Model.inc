<?php

require_once('for_php7.php');
class knje101Model extends Model
{
    public $cmd;
    public $file;
    public $field      = array();
    public $data_arr = array();
    public $controls = array();
    public $testsubclasscd;
    public $testsubclassnm;
    public $coursecd;
    public $majorcd;
    public $coursecode;
    public $wk;
    public $appli;
    public $lastColumn;//CSVの最後の列の値

    public function knje101Model()
    {
        $this->year     = CTRL_YEAR + 1;
    }

    public function init()
    {
        $this->cmd            = VARS::request("cmd");

        $this->field = array("OUTPUT"           =>  VARS::post("OUTPUT"),           //出力取込種別
                             "YEAR"             =>  VARS::post("YEAR"),             //年度一覧
                             "GRADE_HR_CLASS"   =>  VARS::post("GRADE_HR_CLASS"),   //年組一覧
                             "SHAMEXAMCD"       =>  VARS::post("SHAMEXAMCD"),       //テスト種別
                             "HEADER"           =>  VARS::post("HEADER"),           //ヘッダ有
                             "SHORI_MEI"        =>  VARS::post("SHORI_MEI")         //処理名
                             );

        $this->file = VARS::file("FILE");

        $this->getPropertiesAll(); //プロパティファイルの読込み
        $this->lastColumn = 'DUMMY';
        if ($this->Properties["csv_LastColumn"] != '') {
            $this->lastColumn = $this->Properties["csv_LastColumn"];
        }
    }

    public function &getExecModel()
    {
        //事項時間の制限を無くす
        set_time_limit(0);

        $this->data_arr = array();
        //ファイルの文字列の取得
        $data_arr = $this->getFileString();

        //データの整合性チェック
        if ($this->isDataMatched($data_arr)) {
            $errFlg = $this->checkCsvErrQuery(PROGRAMID);
            if ($errFlg) {
                if ($this->field["SHORI_MEI"] == 1) {
                    knje101Query::insertQuery($this, $data_arr);
                    $this->setMessage("MSG202", "読込み件数：".get_count($data_arr)."件");
                } else {
                    knje101Query::deleteQuery($this, $data_arr);
                    $this->setMessage("MSG202", "削除件数：".get_count($data_arr)."件");
                }
            }
        }
    }

    public function getFileString()
    {
        $temp_arr = array();
        if (!is_uploaded_file($this->file["tmp_name"])) {
            $this->warning = "ファイル名が不正です。";
        } else {
            $db = Query::dbCheckOut();
            $db->autoCommit(false);

            //エラーデータの削除
            $query =  $this->deleteQueryErr(PROGRAMID);
            $result = $db->query($query);

            $fp = fopen($this->file["tmp_name"], "r");

            $i= 0;
            $line = 1;  //行数
            $header = false;
            $primaryKey1 = array();
            $primaryKey1 = $this->getPrimaryKey('SHAMEXAMINATION_DAT');
            $duplicateCsv1 = array();
            while (!feof($fp)) {
                $buffer = fgets($fp, 4096);
                if (trim($buffer) != '') {
                    if (!$header && $this->field["HEADER"] == "on") {
                        $header = true;
                        $line++;
                        continue;
                    }

                    //文字コードを判別、改行コードを削除
                    $buffer = trim(i18n_convert($buffer, i18n_http_output(), "SJIS-win"));
                    $tmp    = common::csv2array($buffer);

                    if ($this->Properties["useCurriculumcd"] == '1') {
                        $data = array( "YEAR"              => $tmp[0],
                                        "SHAMEXAMCD"        => (strlen(trim($tmp[3]))) ? sprintf("%02d", $tmp[3]) : "",
                                        "SCHREGNO"          => (strlen(trim($tmp[4]))) ? sprintf("%08d", $tmp[4]) : "",
                                        "CLASSCD"           => (strlen(trim($tmp[5]))) ? sprintf("%02d", $tmp[5]) : "",
                                        "SCHOOL_KIND"       => $tmp[6],
                                        "CURRICULUM_CD"     => $tmp[7],
                                        "SUBCLASSCD"        => (strlen(trim($tmp[8]))) ? sprintf("%06d", $tmp[8]) : "",
                                        "SUBCLASSNAME"      => $tmp[9],
                                        "SUBCLASSCD_CNT"    => (strlen(trim($tmp[10]))) ? sprintf("%d", $tmp[10]) : "",
                                        "TRADE"             => $tmp[11],
                                        "SCORE"             => (strlen(trim($tmp[12]))) ? number_format($tmp[12], 1) : "",
                                        "PRECEDENCE"        => $tmp[13],
                                        "DEVIATION"         => (strlen(trim($tmp[14]))) ? number_format($tmp[14], 1) : "",
                                        "SCHOOL_PRECEDENCE" => $tmp[15],
                                        "SCHOOL_DEVIATION"  => (strlen(trim($tmp[16]))) ? number_format($tmp[16], 1) : "");
                    } else {
                        $data = array( "YEAR"              => $tmp[0],
                                        "SHAMEXAMCD"        => (strlen(trim($tmp[3]))) ? sprintf("%02d", $tmp[3]) : "",
                                        "SCHREGNO"          => (strlen(trim($tmp[4]))) ? sprintf("%08d", $tmp[4]) : "",
                                        "SUBCLASSCD"        => (strlen(trim($tmp[5]))) ? sprintf("%06d", $tmp[5]) : "",
                                        "SUBCLASSNAME"      => $tmp[6],
                                        "SUBCLASSCD_CNT"    => (strlen(trim($tmp[7]))) ? sprintf("%d", $tmp[7]) : "",
                                        "TRADE"             => $tmp[8],
                                        "SCORE"             => (strlen(trim($tmp[9]))) ? number_format($tmp[9], 1) : "",
                                        "PRECEDENCE"        => $tmp[10],
                                        "DEVIATION"         => (strlen(trim($tmp[11]))) ? number_format($tmp[11], 1) : "",
                                        "SCHOOL_PRECEDENCE" => $tmp[12],
                                        "SCHOOL_DEVIATION"  => (strlen(trim($tmp[13]))) ? number_format($tmp[13], 1) : "");
                    }

                    //エラーチェック
                    if ($this->errCheck($db, $data, $this->getHeadCheck(), $line, $duplicateCsv1, $primaryKey1)) {
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

    //模試データチェック用配列
    public function getHeadCheck()
    {
        if ($this->Properties["useCurriculumcd"] == '1') {
            $data_each = array( "YEAR"              => array(4,"年度"),
                                "SHAMEXAMCD"        => array(2,"試験コード"),
                                "SCHREGNO"          => array(8,"学籍番号"),
                                "CLASSCD"           => array(2,"教科コード"),
                                "SCHOOL_KIND"       => array(1,"学校種別"),
                                "CURRICULUM_CD"     => array(1,"教育課程コード"),
                                "SUBCLASSCD"        => array(6,"科目コード"),
                                "SUBCLASSNAME"      => array(30,"科目名"),
                                "SUBCLASSCD_CNT"    => array(4,"試験科目数"),
                                "TRADE"             => array(90,"業者名"),
                                "SCORE"             => array(6,"得点"),
                                "PRECEDENCE"        => array(6,"全国席次"),
                                "DEVIATION"         => array(6,"全国偏差値"),
                                "SCHOOL_PRECEDENCE" => array(6,"校内席次"),
                                "SCHOOL_DEVIATION"  => array(6,"校内偏差値"));
        } else {
            $data_each = array( "YEAR"              => array(4,"年度"),
                                "SHAMEXAMCD"        => array(2,"試験コード"),
                                "SCHREGNO"          => array(8,"学籍番号"),
                                "SUBCLASSCD"        => array(6,"科目コード"),
                                "SUBCLASSNAME"      => array(30,"科目名"),
                                "SUBCLASSCD_CNT"    => array(4,"試験科目数"),
                                "TRADE"             => array(90,"業者名"),
                                "SCORE"             => array(6,"得点"),
                                "PRECEDENCE"        => array(6,"全国席次"),
                                "DEVIATION"         => array(6,"全国偏差値"),
                                "SCHOOL_PRECEDENCE" => array(6,"校内席次"),
                                "SCHOOL_DEVIATION"  => array(6,"校内偏差値"));
        }

        return $data_each;
    }

    //エラーチェック
    public function errCheck($db, &$data, $data_each, $line, $duplicateCsv1, $primaryKey1)
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

        $hissuSep           = "";
        $existenceSep       = "";
        $numericSep         = "";
        $lengthSep          = "";
        $duplicateCsvSep    = "";

        $dupFlg1 = $this->duplicateCsvCheck($duplicateCsv1, $primaryKey1, $data);
        if ($dupFlg1[0] == 1) {
            $returnFlg = $this->errSet($duplicateCsvErrFlg, $duplicateCsvErrMsg, "※重複(CSV):", $duplicateCsvSep, "重複1(". $dupFlg1[1] ."行目)");
        }

        $buff_target_div = "";
        foreach ($data_each as $key => $val) {
            if ($this->Properties["useCurriculumcd"] == '1') {
                //必須項目チェック
                if (in_array($key, array("YEAR", "SHAMEXAMCD", "SCHREGNO", "CLASSCD", "SCHOOL_KIND", "CURRICULUM_CD", "SUBCLASSCD"))) {
                    if (!strlen(trim($data[$key]))) {
                        $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[1]);
                    }
                }

                //存在チェック
                if (in_array($key, array("SCHOOL_KIND", "CURRICULUM_CD"))) {
                    $query = ($key == "SCHOOL_KIND") ? knje101Query::getNameMst('A023', "NAME1") : knje101Query::getNameMst('Z018', "NAMECD2");
                    $data_col = $db->getCol($query);
                    if (!in_array($data[$key], $data_col)) {
                        $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[1]);
                    }
                }
            } else {
                //必須項目チェック
                if (in_array($key, array("YEAR", "SHAMEXAMCD", "SCHREGNO", "SUBCLASSCD"))) {
                    if (!strlen(trim($data[$key]))) {
                        $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[1]);
                    }
                }
            }

            if ($this->field["SHORI_MEI"] == 1) {
                //文字数チェック
                if (strlen($data[$key]) > $val[0]) {
                    $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[1]);
                }

                //得点チェック
                if (in_array($key, array("SCORE", "DEVIATION", "SCHOOL_DEVIATION"))) {
                    if ($data[$key] != "") {
                        $itema = explode(".", $data[$key]);
                        $itemb = str_replace(".", "", $data[$key]);
                        if ($itemb != "" && !is_numeric($itemb)) {
                            $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, $val[1]);
                        }
                        if (strlen($itema[0]) > 3) {
                            $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[1]);
                        }
                    }
                }

                //数値チェック
                if ($this->Properties["useCurriculumcd"] == '1') {
                    if (in_array($key, array("YEAR", "SHAMEXAMCD", "SCHREGNO", "CLASSCD", "CURRICULUM_CD",
                                             "SUBCLASSCD", "SUBCLASSCD_CNT", "PRECEDENCE", "SCHOOL_PRECEDENCE"))
                    ) {
                        if ($data[$key] != "" && !is_numeric($data[$key])) {
                            $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, $val[1]);
                        }
                    }
                } else {
                    if (in_array($key, array("YEAR", "SHAMEXAMCD", "SCHREGNO", "SUBCLASSCD", "SUBCLASSCD_CNT",
                                            "PRECEDENCE", "SCHOOL_PRECEDENCE"))
                    ) {
                        if ($data[$key] != "" && !is_numeric($data[$key])) {
                            $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, $val[1]);
                        }
                    }
                }
            }
        }
        if (!$returnFlg) {
            $checkError = $duplicateCsvErrMsg.$hissuErrMsg.$existenceErrMsg.$numericErrMsg.$lengthErrMsg;
            $checkError = $this->checkErrLength($checkError);
            knje101Query::insertQueryErr($db, $line, $checkError);
        }
        return $returnFlg;
    }

    public function isDataMatched($data_arr)
    {
        //５千件を超えたら制限エラー
        if (get_count($data_arr) > 5000) {
            $this->setWarning("MSG915", "処理件数を５千件以下にして下さい 処理件数：".get_count($data_arr)."件");
            return false;
        }
        return true;
    }

    //テンプレートの書き出し
    public function outputTmpFile()
    {
        //DB接続
        $db = Query::dbCheckOut();

        $contents  = implode(",", $this->getHeadArray($db))."\n";
        $contents .= implode(",", $this->getExample())."\n";
        $filename = sprintf("%s.csv", "対外模試データ");

        //DB切断
        Query::dbCheckIn($db);

        common::downloadContents($contents, $filename);
        return true;
    }


    //CSV出力
    public function outputDataFile()
    {
        //DB接続
        $db = Query::dbCheckOut();
        $header = array("YEAR"              => "年度",
                        "SHAMEXAMCD"        => "試験コード",
                        "SCHREGNO"          => "学籍番号",
                        "CLASSCD"           => "教科コード",
                        "SCHOOL_KIND"       => (get_count($school_kind) > 0) ? "学校種別(".implode('/', $school_kind).")" : "学校種別(J:中学)",
                        "CURRICULUM_CD"     => "教育課程コード",
                        "SUBCLASSCD"        => "科目コード");
        $contents = "";
        if ($this->field["OUTPUT"] == "3") {
            //エラー
            $tables = array();
            $tables = array("1" => "SHAMEXAMINATION_DAT");
            $dupTable = $this->getDuplicateErrorHeader(PROGRAMID, $tables);
            if (sizeof($dupTable) > 0) {
                $headerErr = $this->duplicateCsvHeader($dupTable, $header);
                $contents .= implode($headerErr, ",")."\n";
            }
            $contents .= "行番号, メッセージ\n";
            $filename = sprintf("%s.csv", "模試エラーデータ");
            $query = $this->selectCsvErrQuery(PROGRAMID);
        } else {
            $label = $db->getRow(knje101Query::getYearSem($this->field["YEAR"]), DB_FETCHMODE_ASSOC);
            $grhr  = $db->getRow(knje101Query::getGradeClass($this, $this->field["GRADE_HR_CLASS"]), DB_FETCHMODE_ASSOC);
            $shcd  = $db->getRow(knje101Query::getShamexamcd($this, $this->field["SHAMEXAMCD"]), DB_FETCHMODE_ASSOC);
            $shcd["LABEL"] = $this->field["SHAMEXAMCD"] ? $shcd["LABEL"] : "全";
            $filename = sprintf("%s.csv", $label["LABEL"].$grhr["LABEL"].$shcd["LABEL"]."模試データ");
            //データ
            if ($this->field["HEADER"] == "on") {
                $contents .= implode(",", $this->getHeadArray($db))."\n";
            }
            $query = knje101Query::getselectDataSQL($this);
        }

        $header_data = false;
        //SQL文発行
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $row = str_replace(",", ".", $row);//データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
            $contents .= implode($row, ",") ."\n";
            $header_data = true;
        }
        $result->free();

        //DB切断
        Query::dbCheckIn($db);
        //ＣＳＶ出力
        if ($header_data) {
            common::downloadContents($contents, $filename);

            return true;
        } else {
            $this->warning = "該当データは、ありません。";
            return false;
        }
    }

    //模試データヘッダ配列
    public function getHeadArray($db)
    {
        if ($this->Properties["useCurriculumcd"] == '1') {
            $school_kind = array();
            $result = $db->query(knje101Query::getNameMst('A023', "NAME1, ABBV1"));
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $school_kind[] = $row["NAME1"].':'.$row["ABBV1"];
            }

            $header = array("YEAR"              => "※年度",
                            "GRADE"             => "年組-番",
                            "NAME"              => "氏名",
                            "SHAMEXAMCD"        => "※試験コード",
                            "SCHREGNO"          => "※学籍番号",
                            "CLASSCD"           => "※教科コード",
                            "SCHOOL_KIND"       => (get_count($school_kind) > 0) ? "※学校種別(".implode('/', $school_kind).")" : "※学校種別(J:中学)",
                            "CURRICULUM_CD"     => "※教育課程コード",
                            "SUBCLASSCD"        => "※科目コード",
                            "SUBCLASSNAME"      => "科目名",
                            "SUBCLASSCD_CNT"    => "試験科目数",
                            "TRADE"             => "業者名",
                            "SCORE"             => "得点",
                            "PRECEDENCE"        => "全国席次",
                            "DEVIATION"         => "全国偏差値",
                            "SCHOOL_PRECEDENCE" => "校内席次",
                            "SCHOOL_DEVIATION"  => "校内偏差値",
                            $this->lastColumn   => $this->lastColumn);
        } else {
            $header = array("YEAR"              => "※年度",
                            "GRADE"             => "年組-番",
                            "NAME"              => "氏名",
                            "SHAMEXAMCD"        => "※試験コード",
                            "SCHREGNO"          => "※学籍番号",
                            "SUBCLASSCD"        => "※科目コード",
                            "SUBCLASSNAME"      => "科目名",
                            "SUBCLASSCD_CNT"    => "試験科目数",
                            "TRADE"             => "業者名",
                            "SCORE"             => "得点",
                            "PRECEDENCE"        => "全国席次",
                            "DEVIATION"         => "全国偏差値",
                            "SCHOOL_PRECEDENCE" => "校内席次",
                            "SCHOOL_DEVIATION"  => "校内偏差値",
                            $this->lastColumn   => $this->lastColumn);
        }

        return $header;
    }

    //模試データヘッダ例
    public function getExample()
    {
        if ($this->Properties["useCurriculumcd"] == '1') {
            $header = array("YEAR"              => "9999",
                            "GRADE"             => "年組-番",
                            "NAME"              => "氏名",
                            "SHAMEXAMCD"        => "99",
                            "SCHREGNO"          => "99999999",
                            "CLASSCD"           => "99",
                            "SCHOOL_KIND"       => "J",
                            "CURRICULUM_CD"     => "9",
                            "SUBCLASSCD"        => "999999",
                            "SUBCLASSNAME"      => "１０桁",
                            "SUBCLASSCD_CNT"    => "9999",
                            "TRADE"             => "３０桁",
                            "SCORE"             => "999.9",
                            "PRECEDENCE"        => "999999",
                            "DEVIATION"         => "999.9",
                            "SCHOOL_PRECEDENCE" => "999999",
                            "SCHOOL_DEVIATION"  => "999.9",
                            $this->lastColumn   => $this->lastColumn);
        } else {
            $header = array("YEAR"              => "9999",
                            "GRADE"             => "年組-番",
                            "NAME"              => "氏名",
                            "SHAMEXAMCD"        => "99",
                            "SCHREGNO"          => "99999999",
                            "SUBCLASSCD"        => "999999",
                            "SUBCLASSNAME"      => "１０桁",
                            "SUBCLASSCD_CNT"    => "9999",
                            "TRADE"             => "３０桁",
                            "SCORE"             => "999.9",
                            "PRECEDENCE"        => "999999",
                            "DEVIATION"         => "999.9",
                            "SCHOOL_PRECEDENCE" => "999999",
                            "SCHOOL_DEVIATION"  => "999.9",
                            $this->lastColumn   => $this->lastColumn);
        }

        return $header;
    }
}
