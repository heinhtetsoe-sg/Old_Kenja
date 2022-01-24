<?php

require_once('for_php7.php');
class knjx093dModel extends Model
{
    public $field      = array();
    public $data       = array();
    public $cmd;
    public $file;
    public $lastColumn;//CSVの最後の列の値

    public function knjx093dModel()
    {
    }

    public function init()
    {
        $this->field = array("OUTPUT"           =>  VARS::post("OUTPUT"),           //出力取込種別
                             "YEAR_SEMESTER"    =>  VARS::post("YEAR_SEMESTER"),    //年度・学期
                             "GRADE_COURSE"     =>  VARS::post("GRADE_COURSE")      //学年・コース
                             );

        $this->cmd = VARS::request("cmd");

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
                knjx093dQuery::insertQueryCsv($this, $this->data_arrH, $this->data_arr);
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
            $checkdata1 = array("YEAR"          => array(4, "年度"),
                                "SEMESTER"      => array(1, "学期"),
                                "GROUPCD"       => array(4, "群コード"),
                                "CHAIRCD"       => array(7, "講座コード"));


            $checkdata2 = array("YEAR"          => array(4, "年度"),
                                "SEMESTER"      => array(1, "学期"),
                                "SCHREGNO"      => array(8, "学籍番号"),
                                "APPDATE"       => array(10,"指定開始日"));

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
            $primaryKey1   = array();
            $duplicateCsv1 = array();
            $primaryKey1   = $this->getPrimaryKey("CHAIR_STD_DAT");
            while (!feof($fp)) {
                $buffer = fgets($fp, 4096);
                if (trim($buffer) != '') {
                    //文字コードを判別、改行コードを削除
                    $buffer = trim(mb_convert_encoding($buffer, "SJIS-win", mb_http_output()));
                    $tmp    = common::csv2array($buffer);
                    if ($line == 1) {
                        $dataH["YEAR"] = (strlen(trim($tmp[1]))) ? sprintf("%04d", $tmp[1]) : "";
                        $dataH["SEMESTER"] = $tmp[3];
                        //選択グループ格納
                        for ($j = 10; $j < get_count($tmp); $j++) {
                            if ($tmp[$j] == $this->lastColumn) {
                                continue;
                            } else {
                                $dataH["GROUPCD"][] = (strlen(trim($tmp[$j]))) ? sprintf("%04d", $tmp[$j]) : "";
                            }
                        }
                        //エラーチェック
                        $this->errCheck1($db, $dataH, $checkdata1, $line);
                    } elseif ($line == 2 || $line == 4) {
                        $line++;
                        continue;
                    } elseif ($line == 3) {
                        //講座コード格納
                        for ($j = 10; $j < get_count($tmp); $j++) {
                            if ($tmp[$j] == $this->lastColumn) {
                                continue;
                            } else {
                                $dataH["CHAIRCD"][] = (strlen(trim($tmp[$j]))) ? sprintf("%07d", $tmp[$j]) : "";
                            }
                        }
                        //エラーチェック
                        $this->errCheck1($db, $dataH, $checkdata1, $line);
                    } else {
                        $data = array("YEAR"            => (strlen(trim($tmp[0])))  ? sprintf("%04d", $tmp[0]) : "",
                                      "SEMESTER"        => (strlen(trim($tmp[1])))  ? sprintf("%01d", $tmp[1]) : "",
                                      "SCHREGNO"        => (strlen(trim($tmp[5])))  ? sprintf("%08d", $tmp[5]) : "",
                                      "APPDATE"         => (strlen(trim($tmp[9])))  ? date("Y-m-d", strtotime($tmp[9])) : "");
                        //チェック格納
                        for ($j = 10; $j < get_count($tmp); $j++) {
                            if ($tmp[$j] == $this->lastColumn) {
                                continue;
                            } else {
                                $data["FLG"][] = (strlen(trim($tmp[$j]))) ? $tmp[$j] : "";
                            }
                        }
                        //エラーチェック
                        if ($this->errCheck2($db, $data, $checkdata2, $line, $dataH["GROUPCD"], $dataH["CHAIRCD"], $duplicateCsv1, $primaryKey1)) {
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

    /******************/
    /* エラーチェック */
    /******************/
    //エラー表示（ポップアップ表示：ヘッダ）
    public function errCheck1($db, $data, $checkdata, $line)
    {
        $returnFlg       = true;
        $hissuErrFlg     = false;    //必須エラーフラグ
        $existenceErrFlg = false;    //存在エラーフラグ
        $numericErrFlg   = false;    //数字エラーフラグ
        $lengthErrFlg    = false;    //レングスエラーフラグ
        $orderErrFlg     = false;    //その他エラーフラグ

        $hissuErrMsg     = "";       //必須エラーメッセージ
        $existenceErrMsg = "";       //存在エラーメッセージ
        $numericErrMsg   = "";       //数字エラーメッセージ
        $lengthErrMsg    = "";       //レングスエラーメッセージ
        $orderErrMsg     = "";       //その他エラーメッセージ

        $hissuSep     = "";
        $existenceSep = "";
        $numericSep   = "";
        $lengthSep    = "";
        $orderSep     = "";
        if ($line == 1) {
            foreach ($checkdata as $key => $val) {
                //必須項目チェック
                if (in_array($key, array("YEAR", "SEMESTER"))) {
                    if (!strlen(trim($data[$key]))) {
                        $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[1]);
                    }
                }
                if ($key == "GROUPCD") {
                    if (!strlen(trim(implode('', (array)$data[$key])))) {
                        $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[1]);
                    }
                }

                //数値チェック
                if (in_array($key, array("YEAR", "SEMESTER"))) {
                    if ($data[$key] != "" && !is_numeric($data[$key])) {
                        $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, $val[1]);
                    }
                }

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

            //項目チェック
            list($year, $semester) = explode('-', $this->field["YEAR_SEMESTER"]);
            if ($data["YEAR"] != $year) {
                $returnFlg = $this->errSet($orderErrFlg, $orderErrMsg, "※固有エラー:", $orderSep, "画面の履修年度と異なります。");
            }
            if ($data["SEMESTER"] != $semester) {
                $returnFlg = $this->errSet($orderErrFlg, $orderErrMsg, "※固有エラー:", $orderSep, "画面の学期と異なります。");
            }

            //群コードの存在チェック
            if (strlen(trim($data["YEAR"])) && strlen(trim($data["SEMESTER"])) && strlen(trim(implode('', (array)$data["GROUPCD"])))) {
                for ($g = 0; $g < get_count($data["GROUPCD"]); $g++) {
                    if (0 == $db->getOne(knjx093dQuery::checkGroupcd($data["YEAR"], $data["SEMESTER"], $data["GROUPCD"][$g]))) {
                        $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $checkdata["GROUPCD"][1]."(".($g+1).")");
                    }
                }
            }
        } elseif ($line == 3) {
            //必須項目チェック
            if (!strlen(trim(implode('', (array)$data["CHAIRCD"])))) {
                $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $checkdata["CHAIRCD"][1]);
            }

            //講座の存在チェック
            if (strlen(trim($data["YEAR"])) && strlen(trim($data["SEMESTER"])) && strlen(trim(implode('', (array)$data["GROUPCD"]))) && strlen(trim(implode('', (array)$data["CHAIRCD"])))) {
                for ($g = 0; $g < get_count($data["CHAIRCD"]); $g++) {
                    if (0 == $db->getOne(knjx093dQuery::checkChaircd($this, $data["YEAR"], $data["SEMESTER"], $data["GROUPCD"][$g], $data["CHAIRCD"][$g]))) {
                        $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $checkdata["CHAIRCD"][1]."(".($g+1).")");
                    }
                }
            }

            //在籍から学籍番号一覧取得
            $this->schList = array();
            $this->schList = $db->getCol(knjx093dQuery::getSchregno($this, $data["YEAR"], $data["SEMESTER"]));
        }
        if (!$returnFlg) {
            $checkError = $hissuErrMsg.$numericErrMsg.$lengthErrMsg.$existenceErrMsg.$orderErrMsg;
            $checkError = $this->checkErrLength($checkError);
            knjx093dQuery::insertQueryErr($db, $line, $checkError);
        }

        return $returnFlg;
    }

    //エラー出力（CSV出力：明細）
    public function errCheck2($db, $data, $checkdata, $line, $groupcd, $chaircd, $duplicateCsv1, $primaryKey1)
    {
        $returnFlg       = true;
        $hissuErrFlg     = false;    //必須エラーフラグ
        $existenceErrFlg = false;    //存在エラーフラグ
        $numericErrFlg   = false;    //数字エラーフラグ
        $lengthErrFlg    = false;    //レングスエラーフラグ
        $otherErrFlg     = false;    //その他エラーフラグ
        $duplicateCsvErrFlg = false;

        $hissuErrMsg     = "";       //必須エラーメッセージ
        $existenceErrMsg = "";       //存在エラーメッセージ
        $numericErrMsg   = "";       //数字エラーメッセージ
        $lengthErrMsg    = "";       //レングスエラーメッセージ
        $otherErrMsg     = "";       //その他エラーメッセージ
        $duplicateCsvErrMsg = "";

        $hissuSep     = "";
        $existenceSep = "";
        $numericSep   = "";
        $lengthSep    = "";
        $otherSep     = "";
        $duplicateCsvSep = "";

        $dupFlg1 = $this->duplicateCsvCheck($duplicateCsv1, $primaryKey1, $data);
        if ($dupFlg1[0] == 1) {
            $returnFlg = $this->errSet($duplicateCsvErrFlg, $duplicateCsvErrMsg, "※重複(CSV):", $duplicateCsvSep, "重複1(". $dupFlg1[1] ."行目)");
        }

        list($year, $semester) = explode('-', $this->field["YEAR_SEMESTER"]);

        $appdate_err = false;
        foreach ($checkdata as $key => $val) {
            //必須項目チェック
            if (in_array($key, array("YEAR","SEMESTER","APPDATE","SCHREGNO"))) {
                if (!strlen(trim($data[$key]))) {
                    $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[1]);
                    if ($key == "APPDATE") {
                        $appdate_err = true;
                    }
                }
            }

            //数値チェック
            if (in_array($key, array("YEAR","SEMESTER","SCHREGNO"))) {
                if ($data[$key] != "" && !is_numeric($data[$key])) {
                    $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, $val[1]);
                }
            }

            //文字数チェック
            if (strlen($data[$key]) > $val[0]) {
                $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[1]);
            }

            //項目チェック
            if ($key == "YEAR" && $data["YEAR"] != $year) {
                $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, "対象年度と異なる");
            }
            if ($key == "SEMESTER" && $data["SEMESTER"] != $semester) {
                $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, "対象学期と異なる");
            }
        }

        //YEAR、SEMESTER、SCHREGNO の入力があった場合
        //SCHREG_REGD_DATにその生徒が登録されているのかチェックする
        if (strlen(trim($data["YEAR"])) && strlen(trim($data["SEMESTER"])) && strlen(trim($data["SCHREGNO"]))) {
            if (!in_array($data["SCHREGNO"], $this->schList)) {
                $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, "学籍番号");
            }
        }

        //名簿に登録された講座取得
        $regd_chaircd = array();
        $year_sem = $data["YEAR"].'-'.$data["SEMESTER"];
        $regd_chaircd = $db->getCol(knjx093dQuery::getChaircd($this, $year_sem, $data["SCHREGNO"]));

        //変更可能な日付範囲取得
        $this->dateRange = array();
        $query = knjx093dQuery::getDateRange($data["YEAR"], $data["SEMESTER"], $data["SCHREGNO"]);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $this->dateRange[$row["CHAIRCD"]] = array($row["SDATE"], $row["EDATE"]);
        }
        $result->free();

        $grp_chaircd = array();
        $date_err_cnt = 0;
        for ($g = 0; $g < get_count($chaircd); $g++) {
            //FLGのある講座を群ごとに格納
            if ($data["FLG"][$g] == "1") {
                $grp_chaircd[$groupcd[$g]][] = $chaircd[$g];
            }

            //FLGが変更された講座の日付範囲がエラーの件数取得
            if (!$appdate_err && (($data["FLG"][$g] == "1" && !in_array($chaircd[$g], $regd_chaircd)) || ($data["FLG"][$g] != "1" && in_array($chaircd[$g], $regd_chaircd)))) {
                list($sdate, $edate) = $this->dateRange[$chaircd[$g]];
                if ($sdate > $data["APPDATE"] || $data["APPDATE"] > $edate) {
                    $date_err_cnt++;
                }
            }
        }
        //日付範囲エラーチェック
        if ($date_err_cnt > 0) {
            $returnFlg = $this->errSet($otherErrFlg, $otherErrMsg, "※その他:", $otherSep, "指定開始日が範囲外");
        }

        //複数講座選択のエラー件数取得
        $chair_err_cnt = 0;
        $tmp_group = "";
        for ($g = 0; $g < get_count($groupcd); $g++) {
            if ($tmp_group != $groupcd[$g]) {
                if (get_count($grp_chaircd[$groupcd[$g]]) > 1) {
                    $chair_err_cnt++;
                }
            }
            $tmp_group = $groupcd[$g];
        }

        //講座の複数選択エラーチェック
        if ($chair_err_cnt > 0) {
            $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, "同一群で複数講座選択");
        }

        if (!$returnFlg) {
            $checkError = $duplicateCsvErrMsg.$hissuErrMsg.$numericErrMsg.$lengthErrMsg.$existenceErrMsg.$otherErrMsg;
            $checkError = $this->checkErrLength($checkError);
            knjx093dQuery::insertQueryErr($db, $line, $checkError);
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
                        "GRADE"         => "学年",
                        "HR_CLASS"      => "組",
                        "ATTENDNO"      => "出席番号",
                        "SCHREGNO"      => "※学籍番号",
                        "NAME"          => "氏名",
                        "COURSE"        => "課程学科コース",
                        "COURSENAME"    => "コース名",
                        "APPDATE"       => "※指定開始日");

        list($year, $semester) = explode('-', $this->field["YEAR_SEMESTER"]);

        $header1 = array('※年度', $year, '※学期', $semester,"","","","","","");

        foreach ($header as $key => $val) {
            $header2[] = "";
            $header3[] = "";
            $header4[] = $val;
        }

        $Hcnt = 0;
        $HeaderData = array();
        $result = $db->query(knjx093dQuery::getHeaderData($this));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $header1[] = $row["GROUPCD"];
            $header2[] = $row["GROUPNAME"];
            $header3[] = $row["CHAIRCD"];
            $header4[] = $row["CHAIRNAME"];
            $HeaderData["CHAIRCD"][] = $row["CHAIRCD"];
            $Hcnt++;
        }
        $result->free();

        $header1[] = $this->lastColumn;
        $header2[] = $this->lastColumn;
        $header3[] = $this->lastColumn;
        $header4[] = $this->lastColumn;

        //ヘッダ例
        $header_rei = array("YEAR"          => $year,
                            "SEMESTER"      => $semester,
                            "GRADE"         => "01",
                            "HR_CLASS"      => "001",
                            "ATTENDNO"      => "001",
                            "SCHREGNO"      => "12345678",
                            "NAME"          => "アルプ太郎",
                            "COURSE"        => "9-999-9999",
                            "COURSENAME"    => "普通科コース未設定",
                            "APPDATE"       => CTRL_DATE);

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
            $headerDuplicate = array("YEAR"     => "年度",
                                     "SEMESTER" => "学期",
                                     "CHAIRCD"  => "講座コード",
                                     "SCHREGNO" => "学籍番号",
                                     "APPDATE"  => "指定開始日");
            $tables = array();
            $tables = array("1" => "CHAIR_STD_DAT");
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
                //講座受講情報取得
                $chrStd = array();
                $query = knjx093dQuery::getChairStdDat($this->field["YEAR_SEMESTER"]);
                $result = $db->query($query);
                while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    $chrStd[$row["SCHREGNO"]][$row["CHAIRCD"]] = $row["CNT"];
                }
                $result->free();

                //データ
                $query = knjx093dQuery::getSchList($this);
                $result = $db->query($query);
                while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    $contents .= implode($row, ",");
                    $sep = ",";
                    //CHAIR_STD_DAT取得
                    for ($i = 0; $i < get_count($HeaderData["CHAIRCD"]); $i++) {
                        $flg = ($chrStd[$row["SCHREGNO"]][$HeaderData["CHAIRCD"][$i]] > 0) ? 1 : "";
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
                $couse = "生徒名簿データ";
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
