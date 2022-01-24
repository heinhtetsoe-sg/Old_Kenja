<?php

require_once('for_php7.php');
class knjd128pModel extends Model
{
    public $field = array();
    public $data  = array();
    public $cmd;
    public $replace_data = array();    //置換で選択された生徒一覧
    public $chaircd = "";
    public $file;
    public $itemMst = array();
    public $lastColumn;//CSVの最後の列の値

    public function knjd128pModel()
    {
    }

    public function init()
    {
        if (VARS::request("PROGRAMID")) {
            $this->field = array();
            unset($this->semester);
            common::GetControlMaster_Fnc($this->control);
        }

        //学期
        if (VARS::request("SEMESTER")) {
            $this->semester = VARS::request("SEMESTER");
        }

        //校種
        $this->field["SCHOOL_KIND"] = "P"; //この画面では校種はP固定
        //科目コード
        $this->field["SUBCLASSCD"] = VARS::post("SUBCLASSCD");

        //講座コード
        if ($this->cmd != "subclasscd" && VARS::post("CHAIRCD")) {
            $this->field["CHAIRCD"] = VARS::post("CHAIRCD");
        } else {
            $this->field["CHAIRCD"] = "";
        }

        $this->cmd = VARS::request("cmd");

        $this->field["HEADER"] =  VARS::post("HEADER");           //ヘッダ有
        $this->field["MOVE_ENTER"] =  VARS::post("MOVE_ENTER");

        if (VARS::get("SUBCLASSCD")) {
            $this->subclasscd = VARS::get("SUBCLASSCD");
        }
        if (VARS::get("CHAIRCD")) {
            $this->chaircd = VARS::get("CHAIRCD");
        }

        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        $this->file = VARS::file("FILE");

        //初期化
        $this->fields = array();
        if (is_array($this->data["SCHREGNO"])) {
            foreach ($this->data["SCHREGNO"] as $schregno) {
                for ($semester = 1; $semester <= 3; $semester++) {
                    if (!isset($this->adminControlFlg[$semester])) {
                        continue;
                    } //値非表示
                    if ($this->adminControlFlg[$semester] != "1") {
                        continue;
                    }  //値は表示、入力は不可

                    $this->fields["OUTPUTS_SCORE".$semester."_".$schregno] = VARS::post("OUTPUTS_SCORE".$semester."_".$schregno);
                    $this->fields["SKILLS_SCORE".$semester."_".$schregno]  = VARS::post("SKILLS_SCORE".$semester."_".$schregno);
                }
                $this->fields["SCHREG_GRADE_".$schregno]  = VARS::post("SCHREG_GRADE_".$schregno);
            }
        }

        $this->lastColumn = 'DUMMY';
        if ($this->Properties["csv_LastColumn"] != '') {
            $this->lastColumn = $this->Properties["csv_LastColumn"];
        }
    }

    //入力項目チェック
    public function validateInput()
    {
        return true;
    }

    public function &getUpdateModel()
    {
        if ($this->validateInput()) {
            knjd128pQuery::update($this);
            $this->setMessage("MSG201");
        }
    }

    /**
     * 実行モデルを返します
    */
    public function &getCsvInputModel()
    {
        //ファイルの文字列の取得
        $this->data_arr = array();
        $this->errData = array();
        $this->data_arr = $this->getFileString();
        $this->inputFields = array();
        foreach ($this->data_arr as $schregno => $schregData) {
            foreach ($schregData as $semesData) {
                $schregno = $semesData["SCHREGNO"];
                $semester = $semesData["SEMESTER"];
                $this->inputFields["OUTPUTS_SCORE".$semester."_".$schregno] = $semesData["OUTPUTS_SCORE"];
                $this->inputFields["SKILLS_SCORE".$semester."_".$schregno]  = $semesData["SKILLS_SCORE"];
            }
        }

        //データの整合性チェック
        if ($this->isDataMatched($this->data_arr)) {
            $erraDatas = "";
            $headerDuplicate = array("YEAR"                 => "年度",
                                    "SEMESTER"              => "学期",
                                    "TESTKINDCD"            => "テスト種別コード",
                                    "TESTITEMCD"            => "テスト項目コード",
                                    "SCORE_DIV"             => "区分",
                                    "CLASSCD"               => "教科コード",
                                    "SCHOOL_KIND"           => "学校校種",
                                    "CURRICULUM_CD"         => "教育課程コード",
                                    "SUBCLASSCD"            => "科目コード",
                                    "SCHREGNO"              => "学籍番号");
            $tables = array();
            $tables = array("1" => "PYP_SCORE_DAT");
            $dupTable = $this->getDuplicateErrorHeaderNoSave(PROGRAMID, $tables, $this->errData);
            if (sizeof($dupTable) > 0) {
                $headerErr = $this->duplicateCsvHeader($dupTable, $headerDuplicate);
            }
            if (sizeof($this->errData) > 0) {
                $erraDatas .= "行番号　メッセージ"."\\n";
            }
            foreach ($this->errData as $errKey => $errVal) {
                $erraDatas .= $errKey."行目　".$errVal["MSG"]."\\n";
            }
            if ($erraDatas != "") {
                $this->setMessage("MSG204", "\\n"."取込みエラー内容:". "\\n".$headerErr["MSGREMARK"]. "\\n".$erraDatas);
            } else {
                $this->setMessage("MSG202", "更新件数".get_count($this->data_arr)."件");
            }
        }
    }

    //取込
    public function getFileString()
    {
        $temp_arr = array();
        if (!is_uploaded_file($this->file["tmp_name"])) {        //HTTP POSTによりアップロードされたファイルかどうかを調べる
            $this->warning = "ファイル名が不正です。";
        } else {
            $checkdata = array();
            $checkdata["YEAR"]               = array(4, "年度");
            $checkdata["SEMESTER"]           = array(1, "学期");
            $checkdata["SUBCLASSCD"]         = array(6, "科目コード");
            $checkdata["SCHREGNO"]           = array(8, "学籍番号");
            $checkdata["OUTPUTS_SCORE"]      = array(1, "Outputs評価");
            $checkdata["SKILLS_SCORE"]       = array(1, "Skills評価");

            $db = Query::dbCheckOut();
            $db->autoCommit(false);

            $fp = fopen($this->file["tmp_name"], "r");
            $i  = 0;
            $line = 1;  //行数
            $header = false;

            //取得主キー
            $primaryKey1 = array();
            $duplicateCsv1 = array();
            $primaryKey1 = $this->getPrimaryKey('PYP_SCORE_DAT');
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
                    $tmp    = str_replace("↓", "\n", $tmp);

                    $data = array();
                    $data["YEAR"]          = $tmp[0];
                    $data["SEMESTER"]      = $tmp[1];
                    $data["SUBCLASSCD"]    = $tmp[2];
                    $data["SCHREGNO"]      = (strlen(trim($tmp[4]))) ? sprintf("%08s", $tmp[4]) : "";
                    $data["OUTPUTS_SCORE"] = $tmp[6];
                    $data["SKILLS_SCORE"]  = $tmp[7];
                    $data["TESTKINDCD"]    = "";
                    $data["TESTITEMCD"]    = "";
                    $data["SCORE_DIV"]     = "";
                    $data["CLASSCD"]       = "";
                    $data["SCHOOL_KIND"]   = "";
                    $data["CURRICULUM_CD"] = "";

                    //エラーチェック
                    if ($this->errCheck($db, $data, $checkdata, $line, $duplicateCsv1, $primaryKey1)) {
                        $temp_arr[$data["SCHREGNO"]][$data["SEMESTER"]] = $data;
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
    public function errCheck($db, $data, $data_each, $line, $duplicateCsv1, $primaryKey1)
    {
        $returnFlg = true;
        $hissuErrFlg     = false;    //必須エラーフラグ
        $numericErrFlg   = false;    //数字エラーフラグ
        $lengthErrFlg    = false;    //レングスエラーフラグ
        $huittiErrFlg    = false;    //不一致エラーフラグ
        $existsErrFlg    = false;    //存在エラーフラグ
        $adminErrFlg     = false;    //入力不可エラーフラグ
        $validValueFlg   = false;    //入力値範囲エラーフラグ
        $duplicateCsvErrFlg = false; //重複(CSV)エラーフラグ

        $hissuErrMsg     = "";       //必須エラーメッセージ
        $numericErrMsg   = "";       //数字エラーメッセージ
        $lengthErrMsg    = "";       //レングスエラーメッセージ
        $huittiErrMsg    = "";       //不一致エラーメッセージ
        $existsErrMsg    = "";       //存在エラーメッセージ
        $adminErrMsg     = "";       //入力不可エラーメッセージ
        $validValueMsg   = "";       //入力値範囲エラーフラグ
        $duplicateCsvErrMsg = "";   //重複(CSV)メッセージ

        $hissuSep      = "";
        $numericSep    = "";
        $lengthSep     = "";
        $huittiSep     = "";
        $existsSep     = "";
        $adminSep      = "";
        $validValueSep = "";
        $duplicateCsvSep= "";

        //重複(CSV)チェック
        $dupFlg1 = $this->duplicateCsvCheck($duplicateCsv1, $primaryKey1, $data);
        if ($dupFlg1[0] == 1) {
            $returnFlg = $this->errSet($duplicateCsvErrFlg, $duplicateCsvErrMsg, "※重複(CSV):", $duplicateCsvSep, "重複1(". $dupFlg1[1] ."行目)");
        }
        foreach ($data_each as $key => $val) {
            //必須項目チェック
            if (in_array($key, array("YEAR", "SEMESTER", "SUBCLASSCD", "SCHREGNO"))) {
                if (!strlen(trim($data[$key]))) {
                    $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[1]);
                }
            }

            //入力可な学期かチェック
            if ($key == "SEMESTER") {
                if (isset($this->adminControlFlg[$data[$key]]) && $this->adminControlFlg[$data[$key]] == "1") {
                    //入力可能条件
                } else {
                    $returnFlg = $this->errSet($adminErrFlg, $adminErrMsg, "※入力不可:", $adminSep, $val[1]);
                }
            }

            //コンボで指定した値と一致するかチェック
            if ($key == "SUBCLASSCD") {
                list($classcd, $schoolKind, $curriculumCd, $subclasscd) = explode("-", $this->field["SUBCLASSCD"]);
                if ($subclasscd != $data[$key]) {
                    $returnFlg = $this->errSet($huittiErrFlg, $huittiErrMsg, "※不一致:", $huittiSep, $val[1]);
                }
            }

            //存在チェック
            if ($key == "SCHREGNO") {
                if (!in_array($data[$key], $this->data["SCHREGNO"])) {
                    $returnFlg = $this->errSet($existsErrFlg, $existsErrMsg, "※存在:", $existsSep, $val[1]);
                }
            }

            //数値チェック
            if (in_array($key, array("YEAR",  "SEMESTER", "SUBCLASSCD", "SCHREGNO", "OUTPUTS_SCORE", "SKILLS_SCORE"))) {
                if ($data[$key] != "" && !is_numeric($data[$key])) {
                    $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, $val[1]);
                }
            }

            //入力範囲チェック
            if (in_array($key, array("OUTPUTS_SCORE", "SKILLS_SCORE"))) {
                //学年取得
                $grade = $this->fields["SCHREG_GRADE_".$data["SCHREGNO"]];
                if (in_array($grade, array("01", "02", "03"))) { //1～3年
                    $validValues = array("1", "2", "3", "4");
                    $errMsg = "(1～4)";
                } else { //それ以外(4～5年)
                    $validValues = array("1", "2", "3", "4", "5");
                    $errMsg = "(1～5)";
                }

                if ($data[$key] != "" && !in_array($data[$key], $validValues)) {
                    $returnFlg = $this->errSet($validValueFlg, $validValueMsg, "※入力範囲{$errMsg}:", $validValueSep, $val[1]);
                }
            }

            //文字数チェック
            if (strlen($data[$key]) > $val[0]) {
                $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[1]);
            }
        }

        if (!$returnFlg) {
            $checkError = $duplicateCsvErrMsg.$hissuErrMsg.$existsErrMsg.$numericErrMsg.$lengthErrMsg.$huittiErrMsg.$adminErrMsg.$validValueMsg;
            $checkError = $this->checkErrLength($checkError);
            $this->errData[$line]["MSG"] = $checkError;
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

    //ＣＳＶ出力
    public function getCsvOutputModel()
    {
        //項目
        $header = array();

        $header["YEAR"]            = "※年度";
        $header["SEMESTER"]        = "※学期";
        $header["SUBCLASSCD"]      = "※科目コード";
        $header["SUBCLASSNAME"]    = "科目名";
        $header["SCHREGNO"]        = "※学籍番号";
        $header["NAME_SHOW"]       = "氏名";
        $header["OUTPUTS_SCORE"]   = "Outputs評価";
        $header["SKILLS_SCORE"]    = "Skills評価";
        $header["TOTAL_SCORE"]     = "総括評価";
        $header[$this->lastColumn] = $this->lastColumn;

        $contents  = "";    //クリア

        if ($this->field["HEADER"] == "on") {
            $contents .= implode($header, ",")."\n";
        }

        //DB接続
        $db = Query::dbCheckOut();

        //データ
        $setSemster = $this->semester;
        $sdate = str_replace("/", "-", $this->control["学期開始日付"][$setSemster]);
        $edate = str_replace("/", "-", $this->control["学期終了日付"][$setSemster]);
        if ($sdate <= CTRL_DATE && CTRL_DATE <= $edate) {
            $execute_date = CTRL_DATE;  //初期値
        } else {
            $execute_date = $edate;     //初期値
        }
        $query = knjd128pQuery::selectQuery($this, $execute_date);
        //SUBCLASSCDセット
        $substrcd = explode("-", $this->field["SUBCLASSCD"]);

        $header_data = false;

        //SQL文発行
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $row = str_replace(",", ".", $row);  //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
            $row = str_replace("\r\n", "↓", $row);
            $row = str_replace("\r", "↓", $row);
            $row = str_replace("\n", "↓", $row);

            if (!isset($this->adminControlFlg[$row["SEMESTER"]])) {
                continue;
            }

            $setData = array();
            $setData["YEAR"]            = CTRL_YEAR;
            $setData["SEMESTER"]        = $row["SEMESTER"];
            $setData["SUBCLASSCD"]      = $substrcd[3];
            $setData["SUBCLASSNAME"]    = $row["SUBCLASSNAME"];
            $setData["SCHREGNO"]        = $row["SCHREGNO"];
            $setData["NAME_SHOW"]       = $row["NAME_SHOW"];
            $setData["OUTPUTS_SCORE"]   = $row["OUTPUTS_SCORE"];
            $setData["SKILLS_SCORE"]    = $row["SKILLS_SCORE"];
            $setData["TOTAL_SCORE"]     = $row["TOTAL_SCORE"];
            $setData[$this->lastColumn] = $this->lastColumn;
            $contents .= implode($setData, ",") ."\n";
            $header_data = true;
        }
        $result->free();
        Query::dbCheckIn($db);

        //ＣＳＶ出力
        if ($header_data) {
            $couse = "PYP成績入力";

            $filename = sprintf("%s.csv", $couse);
            common::downloadContents($contents, $filename, "SJIS-win");
            return true;
        } else {
            $this->warning = "該当データは、ありません。";
            return false;
        }
    }
}
