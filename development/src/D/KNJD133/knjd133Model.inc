<?php

require_once('for_php7.php');

class knjd133Model extends Model
{
    public $field = array();
    public $data  = array();
    public $cmd;
    public $replace_data   = array();    //置換で選択された生徒一覧
    public $chaircd = "";
    public $file;

    public $isKomazawa;    // 駒沢
    public $lastColumn;//CSVの最後の列の値

    /**
     * メンバ変数の初期化
     */
    public function knjd133Model()
    {
    }

    public function init()
    {
        if (VARS::request("PROGRAMID")) {
            $this->field = array();
            common::GetControlMaster_Fnc($this->control);
        }
        //科目コード
        $this->field["SUBCLASSCD"] = VARS::post("SUBCLASSCD");

        //講座コード
        if ($this->cmd != "subclasscd" && VARS::post("CHAIRCD")) {
            $this->field["CHAIRCD"] = VARS::post("CHAIRCD");
        } else {
            $this->field["CHAIRCD"] = "";
        }
        $this->field["HEADER"] = "on";
        $this->cmd = VARS::request("cmd");

        //置換
        if ($this->cmd == "replace_update") {
            $this->replace_data["selectdata"] = VARS::post("selectdata");
            $this->replace_data["field"] = array();

            $this->replace_data["check"] = array(VARS::post("RCHECK0"),
                                                 VARS::post("RCHECK1"),
                                                 VARS::post("RCHECK2"),
                                                 VARS::post("RCHECK3"),
                                                 VARS::post("RCHECK4"));
            $this->replace_data["check_all"] = VARS::post("RCHECK5");

            if (VARS::post("RCHECK0") == "1") {
                $this->replace_data["field"]["TOTALSTUDYACT"]     = VARS::post("TOTALSTUDYACT");
            }
            if (VARS::post("RCHECK1") == "1") {
                $this->replace_data["field"]["TOTALSTUDYTIME"]    = VARS::post("TOTALSTUDYTIME");
            }
            if (VARS::post("RCHECK2") == "1") {
                $this->replace_data["field"]["GRAD_VALUE"]        = VARS::post("GRAD_VALUE");
            }
            if (VARS::post("RCHECK3") == "1") {
                $this->replace_data["field"]["COMP_CREDIT"]       = VARS::post("COMP_CREDIT");
            }
            if (VARS::post("RCHECK4") == "1") {
                $this->replace_data["field"]["GET_CREDIT"]        = VARS::post("GET_CREDIT");
            }
        }

        if (VARS::request("GRADE")) {
            $this->subField["GRADE"] = VARS::request("GRADE");
        }

        if (VARS::get("SUBCLASSCD")) {
            $this->subclasscd = VARS::get("SUBCLASSCD");
        }
        if (VARS::get("CHAIRCD")) {
            $this->chaircd = VARS::get("CHAIRCD");
        }
        unset($this->dataDiv);
        if (VARS::get("DATA_DIV")) {
            $this->dataDiv = VARS::get("DATA_DIV");
        }
        unset($this->targetText);
        if (VARS::get("TARGETTEXT")) {
            $this->targetText = VARS::get("TARGETTEXT");
        }
        unset($this->callFunc);
        if (VARS::get("CALLFUNC")) {
            $this->callFunc = VARS::get("CALLFUNC");
        }

        if ($this->cmd == "back") {
            unset($this->replace_data);
        }

        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        $this->file = VARS::file("FILE");

        //初期化
        $this->fields = array();
        for ($i = 0; $i < get_count($this->data["SCHREGNO"]); $i++) {
            $totalstudyact      = "TOTALSTUDYACT-".$i;
            $totalstudytime     = "TOTALSTUDYTIME-".$i;
            $grad_value         = "GRAD_VALUE-".$i;
            $comp_credit        = "COMP_CREDIT-".$i;
            $get_credit         = "GET_CREDIT-".$i;
            $chk_calc_credit    = "CHK_CALC_CREDIT-".$i;
            $this->fields["TOTALSTUDYACT"][]    = VARS::post($totalstudyact);
            $this->fields["TOTALSTUDYTIME"][]   = VARS::post($totalstudytime);
            $this->fields["GRAD_VALUE"][]       = VARS::post($grad_value);
            $this->fields["COMP_CREDIT"][]      = VARS::post($comp_credit);
            $this->fields["GET_CREDIT"][]       = VARS::post($get_credit);
            $this->fields["CHK_CALC_CREDIT"][]  = VARS::post($chk_calc_credit);
        }

        //学校判定
        $this->isKomazawa = knjd133Query::getSchoolDivName('koma');

        $this->lastColumn = 'DUMMY';
        if ($this->Properties["csv_LastColumn"] != '') {
            $this->lastColumn = $this->Properties["csv_LastColumn"];
        }
    }

    //入力項目チェック
    public function validateInput()
    {
        $studyActMoji = $this->totalStudyText["TOTALSTUDYACT"]["moji"];
        $studyActGyou = $this->totalStudyText["TOTALSTUDYACT"]["gyou"];
        $studyTimeMoji = $this->totalStudyText["TOTALSTUDYTIME"]["moji"];
        $studyTimeGyou = $this->totalStudyText["TOTALSTUDYTIME"]["gyou"];
        for ($i = 0; $i < get_count($this->data["SCHREGNO"]); $i++) {
            if ($this->validate_row_cnt($this->fields["TOTALSTUDYTIME"][$i], ($studyTimeMoji * 2)) > $studyTimeGyou) {
                $this->setWarning("MSG915", "( 評価は{$studyTimeGyou}行までです )");
                return false;
            }
            if ($this->validate_row_cnt($this->fields["TOTALSTUDYACT"][$i], ($studyActMoji * 2)) > $studyActGyou) {
                $this->setWarning("MSG915", "( 学習内容は{$studyActGyou}行までです )");
                return false;
            }
        }
        return true;
    }

    public function &getUpdateModel()
    {
        if ($this->validateInput()) {
            knjd133Query::update($this);
            $this->setMessage("MSG201");
        }
    }

    //一括更新
    public function &getReplaceModel()
    {
        //生徒が選択されてない場合
        if (!strlen($this->replace_data["selectdata"])) {
            $this->setWarning("MSG304", "　　　（生徒）");
            return false;
        }
        //最低ひとつでもチェックがついてる場合のみ更新
        if (!in_array("1", $this->replace_data["check"])) {
            $this->setWarning("MSG304", "　　（チェック）");
            return false;
        }
        //入力チェック
        $studyTimeMoji = $this->totalStudyText["TOTALSTUDYTIME"]["moji"];
        $studyTimeGyou = $this->totalStudyText["TOTALSTUDYTIME"]["gyou"];
        if ($this->validate_row_cnt($this->replace_data["field"]["TOTALSTUDYTIME"], ($studyTimeMoji * 2)) > $studyTimeGyou) {
            $this->setWarning("MSG915", "(評価は{$studyTimeGyou}行までです)");
            return false;
        }
        $studyActMoji = $this->totalStudyText["TOTALSTUDYACT"]["moji"];
        $studyActGyou = $this->totalStudyText["TOTALSTUDYACT"]["gyou"];
        if ($this->validate_row_cnt($this->replace_data["field"]["TOTALSTUDYACT"], ($studyActMoji * 2)) > $studyActGyou) {
            $this->setWarning("MSG915", "(学習内容は{$studyActGyou}行までです)");
            return false;
        }
        //チェックがついている項目があるテーブルのみを更新
        if (in_array("1", $this->replace_data["check"])) {
            knjd133Query::replaceUpdate($this);
        }
        $this->setMessage("MSG201");
        //添え字を０から順序良くする
        $this->replace_data["check"][] = $this->replace_data["check_all"];
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

        //データの整合性チェック
        if ($this->isDataMatched($this->data_arr)) {
            $erraDatas = "";
            $headerDuplicate = array("YEAR"                 => "年度",
                                    "SEMESTER"              => "学期",
                                    "SCHREGNO"              => "学籍番号",
                                    "CLASSCD"               => "教科コード",
                                    "SCHOOL_KIND"           => "学校校種",
                                    "CURRICULUM_CD"         => "教育課程コード",
                                    "SUBCLASSCD"            => "科目コード",
                                    "TESTKINDCD"            => "テスト種別コード",
                                    "TESTITEMCD"            => "テスト項目コード",
                                    "SCORE_DIV"             => "区分");
            $tables          = array();
            $tables = array("1" => "RECORD_TOTALSTUDYTIME_DAT", "2" => "RECORD_SCORE_DAT");
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
            //教育課程対応
            if ($this->Properties["useCurriculumcd"] == '1') {
                $checkdata = array("SCHREGNO"       => array(8,"学籍番号"),
                                   "CLASSCD"        => array(2,"教科コード"),
                                   "SCHOOL_KIND"    => array(2,"学校校種"),
                                   "CURRICULUM_CD"  => array(2,"教育課程コード"),
                                   "SUBCLASSCD"     => array(6,"科目コード"),
                                   "TOTALSTUDYACT"  => array(678,"学習内容"),
                                   "TOTALSTUDYTIME" => array(500,"評価"),
                                   "GRAD_VALUE"     => array(2,"学年評定"),
                                   "COMP_CREDIT"    => array(2,"履修単位"),
                                   "GET_CREDIT"     => array(2,"修得単位"));
            } else {
                $checkdata = array("SCHREGNO"       => array(8,"学籍番号"),
                                   "SUBCLASSCD"     => array(6,"科目コード"),
                                   "TOTALSTUDYACT"  => array(678,"学習内容"),
                                   "TOTALSTUDYTIME" => array(500,"評価"),
                                   "GRAD_VALUE"     => array(2,"学年評定"),
                                   "COMP_CREDIT"    => array(2,"履修単位"),
                                   "GET_CREDIT"     => array(2,"修得単位"));
            }

            $db = Query::dbCheckOut();
            $db->autoCommit(false);

            $fp = fopen($this->file["tmp_name"], "r");
            $i  = 0;
            $line = 1;  //行数
            $header = false;
            $primaryKey1   = array();
            $primaryKey2   = array();
            $duplicateCsv1 = array();
            $duplicateCsv2 = array();
            $primaryKey1   = $this->getPrimaryKey('RECORD_TOTALSTUDYTIME_DAT');
            $primaryKey2   = $this->getPrimaryKey('RECORD_SCORE_DAT');
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
                    
                    //教育課程対応
                    if ($this->Properties["useCurriculumcd"] == '1') {
                        $data = array("SCHREGNO"        => (strlen(trim($tmp[3]))) ? sprintf("%08d", $tmp[3]) : "",
                                      "CLASSCD"         => $tmp[4],
                                      "SCHOOL_KIND"     => $tmp[5],
                                      "CURRICULUM_CD"   => $tmp[6],
                                      "SUBCLASSCD"      => $tmp[7],
                                      "TOTALSTUDYACT"   => $tmp[8],
                                      "TOTALSTUDYTIME"  => $tmp[9],
                                      "GRAD_VALUE"      => $tmp[10],
                                      "COMP_CREDIT"     => $tmp[11],
                                      "GET_CREDIT"      => $tmp[12]);
                    } else {
                        $data = array("SCHREGNO"        => (strlen(trim($tmp[3]))) ? sprintf("%08d", $tmp[3]) : "",
                                      "SUBCLASSCD"      => $tmp[4],
                                      "TOTALSTUDYACT"   => $tmp[5],
                                      "TOTALSTUDYTIME"  => $tmp[6],
                                      "GRAD_VALUE"      => $tmp[7],
                                      "COMP_CREDIT"     => $tmp[8],
                                      "GET_CREDIT"      => $tmp[9]);
                    }

                    //エラーチェック
                    if ($this->errCheck($db, $data, $checkdata, $line, $duplicateCsv1, $duplicateCsv2, $primaryKey1, $primaryKey2)) {
                        $temp_arr[$data["SCHREGNO"]] = $data;
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
    public function errCheck($db, $data, $data_each, $line, $duplicateCsv1, $duplicateCsv2, $primaryKey1, $primaryKey2)
    {
        $returnFlg = true;
        $hissuErrFlg     = false;    //必須エラーフラグ
        $numericErrFlg   = false;    //数字エラーフラグ
        $duplicateCsvErrFlg = false;  //重複エラーフラグ

        $hissuErrMsg     = "";       //必須エラーメッセージ
        $numericErrMsg   = "";       //数字エラーメッセージ
        $duplicateCsvErrMsg = "";       //重複エラーメッセージ

        $hissuSep     = "";
        $existenceSep = "";
        $numericSep   = "";
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
            //教育課程対応
            if ($this->Properties["useCurriculumcd"] == '1') {
                if (in_array($key, array("SCHREGNO", "CLASSCD", "SCHOOL_KIND", "CURRICULUM_CD", "SUBCLASSCD"))) {
                    if (!strlen(trim($data[$key]))) {
                        $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[1]);
                    }
                }
            } else {
                if (in_array($key, array("SCHREGNO", "SUBCLASSCD"))) {
                    if (!strlen(trim($data[$key]))) {
                        $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[1]);
                    }
                }
            }
            
            //数値チェック
            //教育課程対応
            if ($this->Properties["useCurriculumcd"] == '1') {
                if (in_array($key, array("SCHREGNO",  "CLASSCD", "CURRICULUM_CD", "SUBCLASSCD", "GRAD_VALUE", "COMP_CREDIT", "GET_CREDIT"))) {
                    if ($data[$key] != "" && !is_numeric($data[$key])) {
                        $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, $val[1]);
                    }
                }
            } else {
                if (in_array($key, array("SCHREGNO", "SUBCLASSCD", "GRAD_VALUE", "COMP_CREDIT", "GET_CREDIT"))) {
                    if ($data[$key] != "" && !is_numeric($data[$key])) {
                        $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, $val[1]);
                    }
                }
            }
        }

        if (!$returnFlg) {
            $checkError = $duplicateCsvErrMsg.$hissuErrMsg.$numericErrMsg;
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
        //教育課程対応
        if ($this->Properties["useCurriculumcd"] == '1') {
            $header = array("HR_NAME"           => "クラス",
                            "ATTENDNO"          => "出席番号",
                            "NAME_SHOW"         => "氏名",
                            "SCHREGNO"          => "※学籍番号",
                            "CLASSCD"           => "※教科コード",
                            "SCHOOL_KIND"       => "※学校校種",
                            "CURRICULUM_CD"     => "※教育課程コード",
                            "SUBCLASSCD"        => "※科目コード",
                            "TOTALSTUDYACT"     => "学習内容",
                            "TOTALSTUDYTIME"    => "評価",
                            "GRAD_VALUE"        => "学年評定",
                            "COMP_CREDIT"       => "履修単位",
                            "GET_CREDIT"        => "修得単位",
                            $this->lastColumn   => $this->lastColumn);
        } else {
            $header = array("HR_NAME"           => "クラス",
                            "ATTENDNO"          => "出席番号",
                            "NAME_SHOW"         => "氏名",
                            "SCHREGNO"          => "※学籍番号",
                            "SUBCLASSCD"        => "※科目コード",
                            "TOTALSTUDYACT"     => "学習内容",
                            "TOTALSTUDYTIME"    => "評価",
                            "GRAD_VALUE"        => "学年評定",
                            "COMP_CREDIT"       => "履修単位",
                            "GET_CREDIT"        => "修得単位",
                            $this->lastColumn   => $this->lastColumn);
        }

        $contents  = "";    //クリア

        $contents .= implode($header, ",")."\n";

        //DB接続
        $db = Query::dbCheckOut();

        //データ
        $sdate = str_replace("/", "-", $this->control["学期開始日付"][CTRL_SEMESTER]);
        $edate = str_replace("/", "-", $this->control["学期終了日付"][CTRL_SEMESTER]);
        if ($sdate <= CTRL_DATE && CTRL_DATE <= $edate) {
            $execute_date = CTRL_DATE;//初期値
        } else {
            $execute_date = $edate;//初期値
        }
        $query = knjd133Query::selectQuery($this, $execute_date);
        //SUBCLASSCDセット
        //教育課程対応
        if ($this->Properties["useCurriculumcd"] == '1') {
            $substrcd = array();
            $substrcd = explode("-", $this->field["SUBCLASSCD"]);
        }

        $header_data = false;

        //SQL文発行
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $row = str_replace(",", ".", $row);  //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
            $row = str_replace("\r\n", "↓", $row);
            $row = str_replace("\r", "↓", $row);
            $row = str_replace("\n", "↓", $row);
            $setData = array();
            $setData["HR_NAME"] = $row["HR_NAME"];
            $setData["ATTENDNO"] = $row["ATTENDNO"];
            $setData["NAME_SHOW"] = $row["NAME_SHOW"];
            $setData["SCHREGNO"] = $row["SCHREGNO"];
            //教育課程対応
            if ($this->Properties["useCurriculumcd"] == '1') {
                if ($row["CLASSCD"]) {
                    $setData["CLASSCD"]         = $row["CLASSCD"];
                } else {
                    $setData["CLASSCD"]         = $substrcd[0];
                }
                if ($row["SCHOOL_KIND"]) {
                    $setData["SCHOOL_KIND"]     = $row["SCHOOL_KIND"];
                } else {
                    $setData["SCHOOL_KIND"]     = $substrcd[1];
                }
                if ($row["CURRICULUM_CD"]) {
                    $setData["CURRICULUM_CD"]   = $row["CURRICULUM_CD"];
                } else {
                    $setData["CURRICULUM_CD"]   = $substrcd[2];
                }
                if ($row["SUBCLASSCD"]) {
                    $setData["SUBCLASSCD"]   = $row["SUBCLASSCD"];
                } else {
                    $setData["SUBCLASSCD"]   = $substrcd[3];
                }
            } else {
                if ($row["SUBCLASSCD"]) {
                    $setData["SUBCLASSCD"] = $row["SUBCLASSCD"];
                } else {
                    $setData["SUBCLASSCD"]   = $this->field["SUBCLASSCD"];
                }
            }
            $setData["TOTALSTUDYACT"] = $row["TOTALSTUDYACT"];
            $setData["TOTALSTUDYTIME"] = $row["TOTALSTUDYTIME"];
            $setData["GRAD_VALUE"] = $row["GRAD_VALUE"];
            $setData["COMP_CREDIT"] = $row["COMP_CREDIT"];
            $setData["GET_CREDIT"] = $row["GET_CREDIT"];
            $setData[$this->lastColumn] = "";
            $contents .= implode($setData, ",") ."\n";
            $header_data = true;
        }
        $result->free();
        Query::dbCheckIn($db);

        //ＣＳＶ出力
        if ($header_data) {
            $couse = "成績入力（総合的な学習の時間）";

            $filename = sprintf("%s.csv", $couse);
            common::downloadContents($contents, $filename, "SJIS-win");
            return true;
        } else {
            $this->warning = "該当データは、ありません。";
            return false;
        }
    }
}
