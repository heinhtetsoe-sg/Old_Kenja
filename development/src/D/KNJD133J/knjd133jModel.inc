<?php

require_once('for_php7.php');
class knjd133jModel extends Model
{
    public $field = array();
    public $data  = array();
    public $cmd;
    public $replace_data = array();    //置換で選択された生徒一覧
    public $chaircd = "";
    public $file;
    public $lastColumn;//CSVの最後の列の値

    public function knjd133jModel()
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

            $this->replace_data["check"] = array(VARS::post("RCHECK0")
                                               , VARS::post("RCHECK1")
                                               , VARS::post("RCHECK2")
                                            );
            $this->replace_data["check_all"] = VARS::post("RCHECK5");

            if (VARS::post("RCHECK0") == "1") {
                $this->replace_data["field"]["TOTALSTUDYACT"]  = VARS::post("TOTALSTUDYACT");
            }
            if (VARS::post("RCHECK1") == "1") {
                $this->replace_data["field"]["TOTALSTUDYTIME"] = VARS::post("TOTALSTUDYTIME");
            }
            if (VARS::post("RCHECK2") == "1") {
                $this->replace_data["field"]["REMARK1"]        = VARS::post("REMARK1");
            }
        }

        if (VARS::get("SUBCLASSCD")) {
            $this->subclasscd = VARS::get("SUBCLASSCD");
        }
        if (VARS::get("CHAIRCD")) {
            $this->chaircd = VARS::get("CHAIRCD");
        }

        if ($this->cmd == "back") {
            unset($this->replace_data);
        }

        //定型文選択
        if (VARS::get("CHR")) {
            $this->chr = VARS::get("CHR");
        }
        $this->subField["GRADE"] = VARS::post("GRADE");

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
            $this->fields["TOTALSTUDYACT"][]    = VARS::post($totalstudyact);
            $this->fields["TOTALSTUDYTIME"][]   = VARS::post("TOTALSTUDYTIME-".$i);
            $this->fields["REMARK1"][]          = VARS::post("REMARK1-".$i);
        }

        //校種取得
        list($classCd, $this->schoolKind, $cul, $subCd) = explode("-", $this->field["SUBCLASSCD"]);
        $this->schoolKind = ($this->schoolKind != "") ? $this->schoolKind: "H";

        //サイズ初期値
        if ($this->Properties["RECORD_TOTALSTUDYTIME_DAT_TOTALSTUDYACT_SIZE_{$this->schoolKind }"]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["RECORD_TOTALSTUDYTIME_DAT_TOTALSTUDYACT_SIZE_{$this->schoolKind }"]);
            $this->getPro["TOTALSTUDYACT"]["moji"] = (int)trim($moji);
            $this->getPro["TOTALSTUDYACT"]["gyou"] = (int)trim($gyou);
        } else {
            $this->getPro["TOTALSTUDYACT"]["moji"] = 22;
            $this->getPro["TOTALSTUDYACT"]["gyou"] = 5;
        }
        if ($this->Properties["RECORD_TOTALSTUDYTIME_DAT_TOTALSTUDYTIME_SIZE_{$this->schoolKind }"]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["RECORD_TOTALSTUDYTIME_DAT_TOTALSTUDYTIME_SIZE_{$this->schoolKind }"]);
            $this->getPro["TOTALSTUDYTIME"]["moji"] = (int)trim($moji);
            $this->getPro["TOTALSTUDYTIME"]["gyou"] = (int)trim($gyou);
        } else {
            $this->getPro["TOTALSTUDYTIME"]["moji"] = 34;
            $this->getPro["TOTALSTUDYTIME"]["gyou"] = 11;
        }
        if ($this->Properties["RECORD_TOTALSTUDYTIME_DAT_REMARK1_SIZE_{$this->schoolKind }"]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["RECORD_TOTALSTUDYTIME_DAT_REMARK1_SIZE_{$this->schoolKind }"]);
            $this->getPro["REMARK1"]["moji"] = (int)trim($moji);
            $this->getPro["REMARK1"]["gyou"] = (int)trim($gyou);
        } else {
            $this->getPro["REMARK1"]["moji"] = 14;
            $this->getPro["REMARK1"]["gyou"] = 5;
        }


        //「道徳」のフィールド情報を取得
        unset($this->useRemark1);
        $db = Query::dbCheckOut();
        $remark1Info = $db->getRow(knjd133jQuery::getNameMst('A040', '10'), DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        if ($remark1Info) {
            // 「道徳」フィールド名
            $this->remark1FieldName = $remark1Info['ABBV1'];
            //「道徳」の教科コード
            $this->remark1ClassCd = $remark1Info['NAMESPARE1'];
            $this->remark1SchoolKind = $remark1Info['NAMESPARE2'];
            $this->remark1Name = $remark1Info['NAMESPARE3'];
            $this->remark1DataDiv = $remark1Info['NAMECD2'];
            ;
        }
        // 「道徳」の場合
        if ($this->field["SUBCLASSCD"] || $this->subclasscd) {
            $subclass = $this->field["SUBCLASSCD"] ? $this->field["SUBCLASSCD"] : $this->subclasscd;
            list($classCd, $schoolKind, $curriculumCd, $subclassCd) = explode('-', $subclass);
            if ($this->remark1ClassCd == $classCd && $this->remark1SchoolKind == $schoolKind) {
                $this->useRemark1 = "1";
            }
        }

        //学校判定
        $this->isChiyoda = knjd133jQuery::getSchoolDivName('chiyoda');
        $this->isBunkyo = knjd133jQuery::getSchoolDivName('bunkyo');

        $this->lastColumn = 'DUMMY';
        if ($this->Properties["csv_LastColumn"] != '') {
            $this->lastColumn = $this->Properties["csv_LastColumn"];
        }
    }

    //入力項目チェック
    public function validateInput()
    {
        $totalstudyact_label = ($this->isChiyoda) ? "観点" : "学習内容";
        for ($i = 0; $i < get_count($this->data["SCHREGNO"]); $i++) {
            if ($this->validate_row_cnt($this->fields["TOTALSTUDYACT"][$i], ($this->getPro["TOTALSTUDYACT"]["moji"] * 2)) > $this->getPro["TOTALSTUDYACT"]["gyou"]) {
                $this->setWarning("MSG915", "( {$totalstudyact_label}は{$this->getPro["TOTALSTUDYACT"]["gyou"]}行までです )");
                return false;
            }
            if ($this->validate_row_cnt($this->fields["TOTALSTUDYTIME"][$i], ($this->getPro["TOTALSTUDYTIME"]["moji"] * 2)) > $this->getPro["TOTALSTUDYACT"]["gyou"]) {
                $this->setWarning("MSG915", "( 評価は{$this->getPro["TOTALSTUDYTIME"]["gyou"]}行までです )");
                return false;
            }
            if ($this->validate_row_cnt($this->fields["REMARK1"][$i], ($this->getPro["REMARK1"]["moji"] * 2)) > $this->getPro["REMARK1"]["gyou"]) {
                $this->setWarning("MSG915", "( {$this->remark1Name}は{$this->getPro["REMARK1"]["gyou"]}行までです )");
                return false;
            }
        }
        return true;
    }

    public function &getUpdateModel()
    {
        if ($this->validateInput()) {
            knjd133jQuery::update($this);
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
        $totalstudyact_label = ($this->isChiyoda) ? "観点" : "学習内容";
        if ($this->validate_row_cnt($this->replace_data["field"]["TOTALSTUDYACT"], ($this->getPro["TOTALSTUDYACT"]["moji"] * 2)) > $this->getPro["TOTALSTUDYACT"]["gyou"]) {
            $this->setWarning("MSG915", "({$totalstudyact_label}は{$this->getPro["TOTALSTUDYACT"]["gyou"]}行までです)");
            return false;
        }
        if ($this->validate_row_cnt($this->replace_data["field"]["TOTALSTUDYTIME"], ($this->getPro["TOTALSTUDYTIME"]["moji"] * 2)) > $this->getPro["TOTALSTUDYTIME"]["gyou"]) {
            $this->setWarning("MSG915", "(評価は{$this->getPro["TOTALSTUDYTIME"]["gyou"]}行までです)");
            return false;
        }
        if ($this->validate_row_cnt($this->replace_data["field"]["REMARK1"], ($this->getPro["REMARK1"]["moji"] * 2)) > $this->getPro["REMARK1"]["gyou"]) {
            $this->setWarning("MSG915", "({$this->remark1Name}は{$this->getPro["REMARK1"]["gyou"]}行までです)");
            return false;
        }
        //チェックがついている項目があるテーブルのみを更新
        if (in_array("1", $this->replace_data["check"])) {
            knjd133jQuery::replaceUpdate($this);
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
                                    "CLASSCD"               => "教科コード",
                                    "SCHOOL_KIND"           => "学校校種",
                                    "CURRICULUM_CD"         => "教育課程コード",
                                    "SUBCLASSCD"            => "科目コード",
                                    "SCHREGNO"              => "学籍番号");
            $tables = array();
            $tables = array("1" => "RECORD_TOTALSTUDYTIME_DAT");
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
            $checkdata["SCHREGNO"]          = array(8,  "学籍番号");
            //教育課程対応
            if ($this->Properties["useCurriculumcd"] == '1') {
                $checkdata["CLASSCD"]           = array(2,  "教科コード");
                $checkdata["SCHOOL_KIND"]       = array(2,  "学校校種");
                $checkdata["CURRICULUM_CD"]     = array(2,  "教育課程コード");
            }
            $checkdata["SUBCLASSCD"]        = array(6,  "科目コード");
            if ($this->useRemark1) {
                $checkdata["REMARK1"]     = array(678, $this->remark1Name);
            } else {
                if ($this->Properties["useTotalstudyTime_J"] != '2') {
                    $checkdata["TOTALSTUDYACT"]     = array(678,"学習内容");
                }
                if ($this->Properties["useTotalstudyTime_J"] == '1' || $this->Properties["useTotalstudyTime_J"] == '2') {
                    $checkdata["TOTALSTUDYTIME"]    = array(500,"評価");
                }
            }

            $db = Query::dbCheckOut();
            $db->autoCommit(false);

            $fp = fopen($this->file["tmp_name"], "r");
            $i  = 0;
            $line = 1;  //行数
            $header = false;

            //取得主キー
            $primaryKey1 = array();
            $duplicateCsv1 = array();
            $primaryKey1 = $this->getPrimaryKey('RECORD_TOTALSTUDYTIME_DAT');
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
                    $data["SCHREGNO"]        = (strlen(trim($tmp[3]))) ? sprintf("%08d", $tmp[3]) : "";
                    //教育課程対応
                    if ($this->Properties["useCurriculumcd"] == '1') {
                        $data["CLASSCD"]         = $tmp[4];
                        $data["SCHOOL_KIND"]     = $tmp[5];
                        $data["CURRICULUM_CD"]   = $tmp[6];
                    }
                    $data["SUBCLASSCD"]      = $tmp[7];
                    $num = 7;
                    if ($this->useRemark1) {
                        $data["REMARK1"]   = $tmp[++$num];
                    } else {
                        if ($this->Properties["useTotalstudyTime_J"] != '2') {
                            $data["TOTALSTUDYACT"]   = $tmp[++$num];
                        }
                        if ($this->Properties["useTotalstudyTime_J"] == '1' || $this->Properties["useTotalstudyTime_J"] == '2') {
                            $data["TOTALSTUDYTIME"]   = $tmp[++$num];
                        }
                    }
                    $data["YEAR"]           = CTRL_YEAR;
                    $data["SEMESTER"]       = $this->semester;

                    //エラーチェック
                    if ($this->errCheck($db, $data, $checkdata, $line, $duplicateCsv1, $primaryKey1)) {
                        $temp_arr[$data["SCHREGNO"]] = $data;
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
        $duplicateCsvErrFlg = false; //重複(CSV)エラーフラグ

        $hissuErrMsg     = "";       //必須エラーメッセージ
        $numericErrMsg   = "";       //数字エラーメッセージ
        $duplicateCsvErrMsg = "";    //重複(CSV)メッセージ

        $hissuSep     = "";
        $numericSep   = "";
        $duplicateCsvSep = "";

        //重複(CSV)チェック
        $dupFlg1 = $this->duplicateCsvCheck($duplicateCsv1, $primaryKey1, $data);
        if ($dupFlg1[0] == 1) {
            $returnFlg = $this->errSet($duplicateCsvErrFlg, $duplicateCsvErrMsg, "※重複(CSV):", $duplicateCsvSep, "重複1(". $dupFlg1[1] ."行目)");
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
                if (in_array($key, array("SCHREGNO",  "CLASSCD", "CURRICULUM_CD", "SUBCLASSCD"))) {
                    if ($data[$key] != "" && !is_numeric($data[$key])) {
                        $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, $val[1]);
                    }
                }
            } else {
                if (in_array($key, array("SCHREGNO", "SUBCLASSCD"))) {
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
        $header = array();
        $header["HR_NAME"]          = "クラス";
        $header["ATTENDNO"]         = "出席番号";
        $header["NAME_SHOW"]        = "氏名";
        $header["SCHREGNO"]         = "※学籍番号";
        //教育課程対応
        if ($this->Properties["useCurriculumcd"] == '1') {
            $header["CLASSCD"]          = "※教科コード";
            $header["SCHOOL_KIND"]      = "※学校校種";
            $header["CURRICULUM_CD"]    = "※教育課程コード";
        }
        $header["SUBCLASSCD"]       = "※科目コード";
        if ($this->useRemark1) {
            $header["REMARK1"]    = $this->remark1Name;
        } else {
            if ($this->Properties["useTotalstudyTime_J"] != '2') {
                $header["TOTALSTUDYACT"]    = "学習内容";
            }
            if ($this->Properties["useTotalstudyTime_J"] == '1' || $this->Properties["useTotalstudyTime_J"] == '2') {
                $header["TOTALSTUDYTIME"]    = "評価";
            }
        }
        $header[$this->lastColumn]           = $this->lastColumn;

        $contents  = "";    //クリア

        $contents .= implode($header, ",")."\n";

        //DB接続
        $db = Query::dbCheckOut();

        //データ
        if ($this->Properties["KNJD133J_semesCombo"] == "1") {
            $setSemster = $this->semester;
        } else {
            $setSemster = CTRL_SEMESTER;
        }
        $sdate = str_replace("/", "-", $this->control["学期開始日付"][$setSemster]);
        $edate = str_replace("/", "-", $this->control["学期終了日付"][$setSemster]);
        if ($sdate <= CTRL_DATE && CTRL_DATE <= $edate) {
            $execute_date = CTRL_DATE;  //初期値
        } else {
            $execute_date = $edate;     //初期値
        }
        $query = knjd133jQuery::selectQuery($this, $execute_date);
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
            $setData["HR_NAME"]     = $row["HR_NAME"];
            $setData["ATTENDNO"]    = $row["ATTENDNO"];
            $setData["NAME_SHOW"]   = $row["NAME_SHOW"];
            $setData["SCHREGNO"]    = $row["SCHREGNO"];
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
                    $setData["SUBCLASSCD"]   = $row["SUBCLASSCD"];
                } else {
                    $setData["SUBCLASSCD"]   = $this->field["SUBCLASSCD"];
                }
            }
            if ($this->useRemark1) {
                $setData["REMARK1"]   = $row[$this->remark1FieldName];
            } else {
                if ($this->Properties["useTotalstudyTime_J"] != '2') {
                    $setData["TOTALSTUDYACT"]   = $row["TOTALSTUDYACT"];
                }
                if ($this->Properties["useTotalstudyTime_J"] == '1' || $this->Properties["useTotalstudyTime_J"] == '2') {
                    $setData["TOTALSTUDYTIME"]  = $row["TOTALSTUDYTIME"];
                }
            }
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
