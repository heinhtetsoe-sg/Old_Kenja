<?php

require_once('for_php7.php');
class knjz401mModel extends Model
{
    public $field  = array();
    public $cmd;
    public $grade_semes = "";  //primary
    public $subclasscd = "";   //primary
    public $file;
    public $lastColumn;//CSVの最後の列の値

    //メンバ変数の初期化
    public function knjz401mModel()
    {
    }

    public function init()
    {
        if (VARS::get("PROGRAMID")) {
            unset($this->grade_semes);
            unset($this->subclasscd);
        }

        $this->cmd = VARS::request("cmd");

        //プロパティファイル
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        $this->file = VARS::file("FILE");

        if (VARS::get("SUBCLASSCD") != "") {
            $this->subclasscd = VARS::get("SUBCLASSCD");
        }
        if (VARS::post("GRADE_SEMES") != "") {
            $this->grade_semes = VARS::post("GRADE_SEMES");
        }
        if (VARS::post("R_GRADE_SEMES") != "") {
            $this->r_grade_semes = VARS::post("R_GRADE_SEMES");
        }

        $this->field = array("HEADER"   => VARS::post("HEADER"),
                             "OUTPUT"   => VARS::post("OUTPUT")
                             );

        $db = Query::dbCheckOut();
        $grade_semes = $db->getOne(knjz401mQuery::getGradeSemes($this));
        $this->grade_semes = ($this->grade_semes) ? $this->grade_semes : $grade_semes;

        $result = $db->query(knjz401mQuery::getData($this, $this->grade_semes, $this->subclasscd));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $this->field["REMARK1_".$row["VIEWCD"]] = VARS::post("REMARK1_".$row["VIEWCD"]);
        }
        Query::dbCheckIn($db);

        $this->lastColumn = 'DUMMY';
        if ($this->Properties["csv_LastColumn"] != '') {
            $this->lastColumn = $this->Properties["csv_LastColumn"];
        }
    }

    //入力項目チェック
    public function validateInput()
    {
        //必須チェック
        if ($this->grade_semes == "") {
            $this->setWarning("MSG310", "（学年学期）");
            return false;
        }
        if ($this->subclasscd == "") {
            $this->setWarning("MSG310", "（教科）");
            return false;
        }

        //サイズチェック
        $db = Query::dbCheckOut();
        $result = $db->query(knjz401mQuery::getData($this, $this->grade_semes, $this->subclasscd));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if ($this->validate_row_cnt($this->field["REMARK1_".$row["VIEWCD"]], (20 * 2)) > 5) {
                $this->setWarning("MSG915", "(めあては5行までです)");
                return false;
            }
        }
        Query::dbCheckIn($db);

        return true;
    }

    //更新用のモデルを返します
    public function &getUpdateModel()
    {
        //入力チェック
        if ($this->validateInput()) {
            $this->update();
        }
    }

    //更新
    public function &update()
    {
        knjz401mQuery::getUpdateQuery($this, $this->field);
        if (!isset($this->warning)) {
            $this->setMessage("MSG201");
        }
        return;
    }

    //コピー
    public function &getCopyModel()
    {
        $db = Query::dbCheckOut();
        $saki_data  = $db->getOne(knjz401mQuery::checkData($this, $this->grade_semes, $this->r_grade_semes, "saki", CTRL_YEAR));
        $moto_data  = $db->getOne(knjz401mQuery::checkData($this, $this->r_grade_semes, $this->grade_semes, "moto", CTRL_YEAR));
        Query::dbCheckIn($db);

        //入力チェック
        if (!$this->grade_semes || !$this->r_grade_semes) {
            $this->setWarning("MSG304", "（学年学期）");
        } elseif ($this->grade_semes == $this->r_grade_semes) {
            $this->setWarning("MSG901", "学年学期が同じです。");
        } elseif ($saki_data > 0) {
            $this->setWarning("対象学年学期にデータが存在しています。");
        } elseif ($moto_data == 0) {
            $this->setWarning("参照学年学期にデータがありません。");
        } else {
            knjz401mQuery::getCopyQuery($this);
            $this->setMessage("MSG201");
        }
        return;
    }


    //前年度コピー
    public function &getPreCopyModel()
    {
    
        //前年度のデータ取得
        $pre_year = CTRL_YEAR - 1;

        $db = Query::dbCheckOut();
        $saki_data  = $db->getOne(knjz401mQuery::checkData($this, $this->grade_semes, $this->r_grade_semes, "pre_copy", CTRL_YEAR)); //今年度データ
        $moto_data  = $db->getOne(knjz401mQuery::checkData($this, $this->grade_semes, $this->r_grade_semes, "pre_copy", $pre_year)); //前年度データ
        Query::dbCheckIn($db);

        //入力チェック
        if ($saki_data > 0) {
            $this->setWarning("今年度のデータが存在します。");
        } elseif ($moto_data == 0) {
            $this->setWarning("前年度のデータがありません。");
        } else {
            knjz401mQuery::getPreCopyQuery($this);
            $this->setMessage("MSG201");
        }
        return;
    }

    /*************/
    /*  CSV出力  */
    /*************/
    public function getDownloadCsvModel()
    {
        $duplHeader = array("YEAR"         => "年度",
                            "SEMESTER"     => "学期",
                            "GRADE"        => "学年",
                            "CLASSCD"      => "教科コード",
                            "SCHOOL_KIND"  => "学校校種",
                            "CURRICULUM_CD"=> "教育課程コード",
                            "SUBCLASSCD"   => "教科コード",
                            "VIEWCD"       => "観点コード"
        );
        $db = Query::dbCheckOut();
        $data = false;

        if ($this->field["OUTPUT"] == "3") {        //エラー出力
            //ヘッダー（エラー出力）
            $header_err = array("MSGROW"    => "行番号",
                                "MSGREMARK" => "メッセージ");
            //エラー
            $tables = array();
            $tables = array("1" => "JVIEWNAME_GRADE_SEMES_DETAIL_MST");
            $dupTable = $this->getDuplicateErrorHeader(PROGRAMID, $tables);
            if (sizeof($dupTable) > 0) {
                $headerErr = $this->duplicateCsvHeader($dupTable, $duplHeader);
                $contents .= implode($headerErr, ",")."\n";
            }
            //ヘッダをセット
            $contents .= implode(",", $header_err) . "\r\n";

            //エラー出力データ
            $query = $this->selectCsvErrQuery(PROGRAMID);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $row = str_replace("\r\n", "", str_replace(",", ".", $row));   //データ内にカンマがあれば置換、置換する文字はカンマでなければ何でも良い。
                $contents .= implode($row, ",") ."\n";
                $data = true;
            }
            $result->free();
        } elseif ($this->field["OUTPUT"] == "4") {     //ヘッダ出力（見本）
            //ヘッダー
            $header = array();
            $header["GRADE"]        = "※学年";
            $header["SEMESTER"]     = "※学期";
            $header["SUBCLASSCD"]   = "※教科コード";
            $header["SUBCLASSNAME"] = "教科名";
            $header["VIEWCD"]       = "観点コード";
            $header["VIEWABBV"]     = "観点";
            $header["REMARK1"]      = "めあて";

            //科目データ取得
            $subcls = $db->getRow(knjz401mQuery::getSubclassData($this), DB_FETCHMODE_ASSOC);

            //ヘッダー例
            $header_rei = array();
            $header_rei["GRADE"]        = "01";
            $header_rei["SEMESTER"]     = "1";
            $header_rei["SUBCLASSCD"]   = $subcls["VALUE"];
            $header_rei["SUBCLASSNAME"] = $subcls["LABEL"];
            $header_rei["VIEWCD"]       = substr($subcls["VALUE"], 0, 2)."01";
            $header_rei["VIEWABBV"]     = "関心・意欲・態度";
            $header_rei["REMARK1"]      = "全角100文字まで";

            //ヘッダ有チェックボックス
            if ($this->field["HEADER"]) {
                //ヘッダをセット
                $contents = implode(",", $header) . "\r\n";
            } else {
                $contents = "";
            }

            //ヘッダ例をセット
            $contents .= implode(",", $header_rei) . "\r\n";
            $data = true;
        } elseif ($this->field["OUTPUT"] == "2") {     //書出（CSVデータ出力）
            $dataTemp = array();
            $tempCnt = array();

            //CSV出力データ取得
            $query = knjz401mQuery::getCsvData($this);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $row = str_replace(",", ".", $row);   //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。

                $key = $row["GRADE"].$row["SEMESTER"].$row["SUBCLASSCD"];
                if ($dataTemp[$key] == "") {
                    $dataTemp[$key][] = $row["GRADE"];
                    $dataTemp[$key][] = $row["SEMESTER"];
                    $dataTemp[$key][] = $row["SUBCLASSCD"];
                    $dataTemp[$key][] = $row["SUBCLASSNAME"];
                    $dataTemp[$key][] = $row["VIEWCD"];
                    $dataTemp[$key][] = $row["VIEWABBV"];
                    $dataTemp[$key][] = $row["REMARK1"];
                } else {
                    $dataTemp[$key][] = $row["VIEWCD"];
                    $dataTemp[$key][] = $row["VIEWABBV"];
                    $dataTemp[$key][] = $row["REMARK1"];
                }
                $tempCnt[$key]++;
            }
            $result->free();

            //ヘッダー
            $header = array();
            $header["GRADE"]        = "※学年";
            $header["SEMESTER"]     = "※学期";
            $header["SUBCLASSCD"]   = "※教科コード";
            $header["SUBCLASSNAME"] = "教科名";

            //データが出力されるMAX列数を取得
            $headCnt = 0;
            foreach ($tempCnt as $key => $val) {
                $headCnt = ($headCnt < $val) ? $val : $headCnt;
            }
            //MAX列数分のヘッダを取得
            for ($i = 0; $i < $headCnt; $i++) {
                $header["VIEWCD".$i]        = "観点コード".($i+1);
                $header["VIEWABBV".$i]      = "観点".($i+1);
                $header["REMARK1".$i]       = "めあて".($i+1);
            }
            $header[$this->lastColumn] = $this->lastColumn;

            //ヘッダ有チェックボックス
            if ($this->field["HEADER"]) {
                //ヘッダをセット
                $contents = implode(",", $header) . "\r\n";
            } else {
                $contents = "";
            }

            //ＣＳＶデータをセット
            $cnt = 0;
            foreach ($dataTemp as $key => $val) {
                $contents .= implode($val, ",");
                for ($i = get_count($val); $i < get_count($header); $i++) {
                    $contents .= ",";
                }
                $contents .= $this->lastColumn."\r\n";

                $data = true;
                $cnt++;
            }
        }

        Query::dbCheckIn($db);

        if ($data) {
            if ($this->field["OUTPUT"] == "3") {
                $couse = "エラー出力";
            } else {
                $couse = CTRL_YEAR."年度_学年・学期別観点のめあてマスタ";
            }
            $filename = sprintf("%s.csv", $couse);
            common::downloadContents($contents, $filename);
            return true;
        } else {
            $this->setWarning("MSG303");
            return false;
        }
    }

    /*************/
    /*  CSV取込  */
    /*************/
    public function getUploadCsvModel()
    {
        //ファイルの文字列の取得
        $this->data_arr = array();
        $this->data_arr = $this->getFileString();
        //データの整合性チェック
        if ($this->isDataMatched($this->data_arr)) {
            $errFlg = $this->checkCsvErrQuery(PROGRAMID);
            if ($errFlg) {
                $db = Query::dbCheckOut();
                $db->autoCommit(false);
                //更新（処理件数取得）
                knjz401mQuery::insertQueryCsv($db, $this, $this->data_arr);
                $db->commit();
                Query::dbCheckIn($db);
                $this->setMessage("MSG202", "更新件数".get_count($this->data_arr)."件");
            }
        }
    }

    //送られたファイルの文字列を取得
    public function getFileString()
    {
        $temp_arr = array();
        if (!is_uploaded_file($this->file["tmp_name"])) {   //HTTP POSTによりアップロードされたファイルかどうかを調べる
            $this->warning = "ファイル名が不正です。";
        } else {
            //チェック対象項目（フィールド名 => サイズ, 項目名（メッセージ用））
            $checkdata = array("GRADE"          => array(2,   "学年"),
                               "SEMESTER"       => array(1,   "学期"),
                               "SUBCLASSCD"     => array(14,  "教科コード"),
                               "VIEWCD"         => array(4,   "観点コード")
                               );

            $db = Query::dbCheckOut();
            $db->autoCommit(false);

            //エラーデータの削除
            $query = $this->deleteQueryErr(PROGRAMID);
            $db->query($query);

            $fp = fopen($this->file["tmp_name"], "r");
            $i  = 0;
            $line = 1;  //行数
            $header = false;
            $primaryKey1 = array();
            $primaryKey1 = $this->getPrimaryKey('JVIEWNAME_GRADE_SEMES_DETAIL_MST');
            $duplicateCsv1 = array();
            while (!feof($fp)) {
                $buffer = fgets($fp, 4096);
                if (trim($buffer) != '') {
                    //ヘッダ有の場合、1行目をスキップ
                    if (!$header && $this->field["HEADER"] == "on") {
                        $header = true;
                        $line++;
                        continue;
                    }
                    //文字コードを判別、改行コードを削除
                    $buffer = trim(i18n_convert($buffer, i18n_http_output(), "SJIS-win"));
                    $tmp    = common::csv2array($buffer);

                    //データ項目＆値セット
                    $data = array("GRADE"           => (strlen(trim($tmp[0]))) ? sprintf("%02d", $tmp[0]) : "",
                                  "SEMESTER"        => $tmp[1],
                                  "SUBCLASSCD"      => $tmp[2]
                                 );
                    $s_no = 4;

                    $dataTemp[$key][] = $row["GRADE"];
                    $dataTemp[$key][] = $row["SEMESTER"];
                    $dataTemp[$key][] = $row["SUBCLASSCD"];
                    $dataTemp[$key][] = $row["SUBCLASSNAME"];
                    $dataTemp[$key][] = $row["VIEWCD"];
                    $dataTemp[$key][] = $row["VIEWABBV"];
                    $dataTemp[$key][] = $row["REMARK1"];

                    $col_no = 0;
                    $seki = 1;
                    for ($j = $s_no; $j <=get_count($tmp); $j++) {
                        if ($tmp[$j] == $this->lastColumn) {
                            break;
                        }

                        if (3 * $seki + 1 == $j) {
                            $col_no++;
                            $seki++;
                            $data["VIEWCD".$col_no]     = (strlen(trim($tmp[$j]))) ? sprintf("%04d", $tmp[$j]) : "";
                            $data["REMARK1".$col_no]    = ($tmp[$j+2] == $this->lastColumn) ? "" : $tmp[$j+2];
                        }
                    }

                    //チェック対象項目（フィールド名 = サイズ, 項目名（メッセージ用））
                    for ($j = 1; $j <= $col_no; $j++) {
                        $checkdata["VIEWCD".$j]     = array(4,   "観点コード".$j);
                        $checkdata["REMARK1".$j]    = array(308, "めあて".$j);
                    }

                    //エラーチェック
                    if ($this->errCheck($db, $data, $checkdata, $col_no, $line, $duplicateCsv1, $primaryKey1)) {
                        $temp_arr[$i] = $data;
                        $temp_arr[$i]["COL_NO"] = $col_no;
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

    //エラーチェック
    public function errCheck($db, $data, $checkdata, $col_no, $line, $duplicateCsv1, $primaryKey1)
    {
        $returnFlg = true;
        $hissuErrFlg        = false;    //必須エラーフラグ
        $numericErrFlg      = false;    //数字エラーフラグ
        $lengthErrFlg       = false;    //レングスエラーフラグ
        $masterErrFlg       = false;    //マスタエラーフラグ
        $orderErrFlg        = false;    //固有エラーフラグ
        $duplicateCsvErrFlg = false;    //重複エラーフラグ
        $valuesErrFlg       = false;    //値不正エラーフラグ

        $hissuErrMsg        = "";       //必須エラーメッセージ
        $numericErrMsg      = "";       //数字エラーメッセージ
        $lengthErrMsg       = "";       //レングスエラーメッセージ
        $masterErrMsg       = "";       //マスタエラーメッセージ
        $orderErrMsg        = "";       //固有エラーメッセージ
        $duplicateCsvErrMsg = "";       //重複エラーメッセージ
        $valuesErrMsg       = "";       //値不正エラーメッセージ

        $hissuSep        = "";
        $numericSep      = "";
        $lengthSep       = "";
        $masterSep       = "";
        $orderSep        = "";
        $duplicateCsvSep = "";
        $valuesSep       = "";

        $dupFlg1 = $this->duplicateCsvCheck($duplicateCsv1, $primaryKey1, $data);
        if ($dupFlg1[0] == 1) {
            $returnFlg = $this->errSet($duplicateCsvErrFlg, $duplicateCsvErrMsg, "※重複(CSV):", $duplicateCsvSep, "重複1(". $dupFlg1[1] ."行目)");
        }
        $viewcd = $remark1 = array();
        for ($i = 1; $i <= $col_no; $i++) {
            $viewcd[]   = "VIEWCD".$i;
            $remark1[]  = "REMARK1".$i;
        }

        //必須項目チェック
        if (!$col_no) {
            $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, "観点コード");
        }
        foreach ($checkdata as $key => $val) {
            //必須項目チェック
            if (in_array($key, array_merge(array("GRADE","SEMESTER","SUBCLASSCD")))) {
                if (!strlen(trim($data[$key]))) {
                    $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[1]);
                }
            }

            //数値チェック
            if (in_array($key, array_merge(array("GRADE","SEMESTER"), $viewcd))) {
                if ($data[$key] != '' && !is_numeric($data[$key])) {
                    $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, $val[1]);
                }
            }

            //文字数チェック
            if (strlen($data[$key]) > $val[0]) {
                $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[1]);
            }

            //存在チェック
            if ($key == "SUBCLASSCD") {
                if (substr($data["SUBCLASSCD"], 0, 2) > "91") {
                    $returnFlg = $this->errSet($valuesErrFlg, $valuesErrMsg, "※値不正:", $valuesSep, "科目コードの先頭２桁が91より大きい");
                }
            }
            if (in_array($key, array_merge($viewcd))) {
                if ($data[$key] != "" && substr($data[$key], 0, 2) != substr($data["SUBCLASSCD"], 0, 2)) {
                    $returnFlg = $this->errSet($orderErrFlg, $orderErrMsg, "※固有エラー:", $orderSep, $val[1]."と科目コードの先頭２桁が不一致");
                }
            }
        }

        //存在チェック
        $chk_data = array();
        $chk_data[] = array("QUERY"    => knjz401mQuery::checkGrade($this, $data),
                            "MSG"      => "学年");
        $chk_data[] = array("QUERY"    => knjz401mQuery::checkSemester($this, $data),
                            "MSG"      => "学期");
        $chk_data[] = array("QUERY"    => knjz401mQuery::checkSubclasscd($this, $data),
                            "MSG"      => "科目コード");
        foreach ($viewcd as $key => $val) {
            if ($data[$val] != "") {
                $chk_data[] = array("QUERY"    => knjz401mQuery::checkViewcd($this, $data, $data[$val]),
                                    "MSG"      => "観点コード");
            }
        }
        for ($i=0; $i < get_count($chk_data); $i++) {
            if ($db->getOne($chk_data[$i]["QUERY"]) == "0") {
                $returnFlg = $this->errSet($masterErrFlg, $masterErrMsg, "※マスタチェック:", $masterSep, $chk_data[$i]["MSG"]);
            }
        }
        if (!$returnFlg) {
            $checkError = $duplicateCsvErrMsg.$hissuErrMsg.$numericErrMsg.$lengthErrMsg.$orderErrMsg.$valuesErrMsg.$masterErrMsg;
            $checkError = $this->checkErrLength($checkError);
            knjz401mQuery::insertQueryErr($db, $line, $checkError);
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
}
