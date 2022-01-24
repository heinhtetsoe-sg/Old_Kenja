<?php

require_once('for_php7.php');
class knjd133cModel extends Model
{
    public $field = array();
    public $data  = array();
    public $cmd;
    public $replace_data = array();    //置換で選択された生徒一覧
    public $chaircd = "";
    public $file;
    public $itemMst = array();
    public $lastColumn;//CSVの最後の列の値

    public function knjd133cModel()
    {
    }

    public function init()
    {
        if (VARS::request("PROGRAMID")) {
            $this->field = array();
            unset($this->semester);
            common::GetControlMaster_Fnc($this->control);
            $this->itemMst = array();
            //一括更新画面で使用する値をリセット
            unset($this->replace_data);
            unset($this->itemMstJson);
        }

        //学期
        if (VARS::request("SEMESTER")) {
            $this->semester = VARS::request("SEMESTER");
        }

        //校種
        $this->field["SCHOOL_KIND"] = VARS::post("SCHOOL_KIND");
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

        if ($this->cmd == 'subclasscd') {
            $this->itemMst = array();
        }
        if ($this->field["SUBCLASSCD"]) {
            $this->itemMst = array();
            $query = knjd133cQuery::getItemMst($this->field["SUBCLASSCD"]);
            $db = Query::dbCheckOut();
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                if ($row["DATA_SIZE"]) {
                    list($moji, $gyou) = preg_split("/\*/", $row["DATA_SIZE"]);
                    $row["moji"] = $moji + 0;
                    $row["gyou"] = $gyou + 0;
                }
                if ($row["ITEMNAME"] == "") {
                    if ($row["COLUMNNAME"] == 'TOTALSTUDYACT') {
                        $row["ITEMNAME"] = "学習内容";
                    } elseif ($row["COLUMNNAME"] == 'TOTALSTUDYTIME') {
                        $row["ITEMNAME"] = "評価";
                    } elseif ($row["COLUMNNAME"] == 'REMARK1') {
                        $row["ITEMNAME"] = "観点";
                    }
                }
                $this->itemMst[] = $row;
            }
            $result->free();
            Query::dbCheckIn($db);

            $arr2 = array();
            for ($i = 0; $i < get_count($this->itemMst); $i++) {
                $arr = array();
                foreach ($this->itemMst[$i] as $key => $val) {
                    if (in_array($key, array("COLUMNNAME", "moji", "gyou"))) {
                        $arr[] = "\"{$key}\": \"{$val}\"";
                    }
                }
                $arr2[] = "\"{$this->itemMst[$i]["COLUMNNAME"]}\": {".implode(",", $arr)."}";
            }
            $this->itemMstJson ="{".implode(",", $arr2)."}";
        }

        //置換
        if ($this->cmd == "replace_update") {
            $this->replace_data["selectdata"] = VARS::post("selectdata");
            $this->replace_data["field"] = array();
            $this->replace_data["check"] = array();
            foreach ($this->itemMst as $m) {
                $this->replace_data["field"][$m["COLUMNNAME"]] = VARS::post($m["COLUMNNAME"]);
                $this->replace_data["check"][$m["COLUMNNAME"]] = VARS::post("RCHECK_".$m["COLUMNNAME"]);
            }
            $this->replace_data["check_all"] = VARS::post("RCHECK5");
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
        if ($this->cmd == "teikei") {
            $this->columnname = VARS::get("COLUMNNAME");
            $this->patternDataDiv = VARS::get("PATTERN_DATA_DIV");
            $this->itemname = VARS::get("ITEMNAME");
            $this->targetid = VARS::get("TARGETID");
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
            foreach ($this->itemMst as $m) {
                $this->fields[$m["COLUMNNAME"]][]    = VARS::post($m["COLUMNNAME"]."-".$i);
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
        for ($i = 0; $i < get_count($this->data["SCHREGNO"]); $i++) {
            foreach ($this->itemMst as $m) {
                $itemname = $m["ITEMNAME"];
                $columnname = $m["COLUMNNAME"];
                if ($this->validate_row_cnt($this->fields[$columnname][$i], ($m["moji"] * 2)) > $m["gyou"]) {
                    $this->setWarning("MSG915", "( {$itemname}は{$m["gyou"]}行までです )");
                    return false;
                }
            }
        }
        return true;
    }

    public function &getUpdateModel()
    {
        if ($this->validateInput()) {
            knjd133cQuery::update($this);
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
        if (!in_array("1", array_values($this->replace_data["check"]))) {
            $this->setWarning("MSG304", "　　（チェック）");
            return false;
        }
        //入力チェック
        foreach ($this->itemMst as $m) {
            $columnname = $m["COLUMNNAME"];
            $itemname = $m["ITEMNAME"];
            if ($this->validate_row_cnt($this->replace_data["field"][$columnname], ($m["moji"] * 2)) > $m["gyou"]) {
                $this->setWarning("MSG915", "( {$itemname}は{$m["gyou"]}行までです )");
                return false;
            }
        }
        //チェックがついている項目があるテーブルのみを更新
        if (in_array("1", array_values($this->replace_data["check"]))) {
            knjd133cQuery::replaceUpdate($this);
        }
        $this->setMessage("MSG201");
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
            $headerDuplicate = array();
            $headerDuplicate["YEAR"]              = "学年";
            $headerDuplicate["SEMESTER"]          = "学期";
            $headerDuplicate["SCHREGNO"]          = "学籍番号";
            $headerDuplicate["CLASSCD"]           = "教科コード";
            $headerDuplicate["SCHOOL_KIND"]       = "学校校種";
            $headerDuplicate["CURRICULUM_CD"]     = "教育課程コード";
            $headerDuplicate["SUBCLASSCD"]        = "科目コード";
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
            $checkdata["CLASSCD"]           = array(2,  "教科コード");
            $checkdata["SCHOOL_KIND"]       = array(2,  "学校校種");
            $checkdata["CURRICULUM_CD"]     = array(2,  "教育課程コード");
            $checkdata["SUBCLASSCD"]        = array(6,  "科目コード");
            foreach ($this->itemMst as $m) {
                $checkdata[$m["COLUMNNAME"]]     = array("LENGTH", $m["ITEMNAME"], $m["moji"], $m["gyou"]);
            }


            $db = Query::dbCheckOut();
            $db->autoCommit(false);

            $fp = fopen($this->file["tmp_name"], "r");
            $i  = 0;
            $line = 1;  //行数
            $header = false;
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
                    $data["CLASSCD"]         = $tmp[4];
                    $data["SCHOOL_KIND"]     = $tmp[5];
                    $data["CURRICULUM_CD"]   = $tmp[6];
                    $data["SUBCLASSCD"]      = $tmp[7];
                    $num = 7;
                    foreach ($this->itemMst as $m) {
                        $data[$m["COLUMNNAME"]]   = $tmp[++$num];
                    }

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
        $lengthErrFlg    = false;    //レングスエラーフラグ
        $duplicateCsvErrFlg = false;    //重複CSV

        $hissuErrMsg     = "";       //必須エラーメッセージ
        $numericErrMsg   = "";       //数字エラーメッセージ
        $lengthErrMsg    = "";       //レングスエラーメッセージ
        $duplicateCsvErrMsg = "";   //重複CSV

        $hissuSep     = "";
        $numericSep   = "";
        $lengthSep    = "";
        $duplicateCsvSep = "";

        $dupFlg1 = $this->duplicateCsvCheck($duplicateCsv1, $primaryKey1, $data);
        if ($dupFlg1[0] == 1) {
            $returnFlg = $this->errSet($duplicateCsvErrFlg, $duplicateCsvErrMsg, "※重複(CSV):", $duplicateCsvSep, "重複1(". $dupFlg1[1] ."行目)");
        }

        foreach ($data_each as $key => $val) {
            //必須項目チェック
            if (in_array($key, array("SCHREGNO", "CLASSCD", "SCHOOL_KIND", "CURRICULUM_CD", "SUBCLASSCD"))) {
                if (!strlen(trim($data[$key]))) {
                    $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[1]);
                }
            }

            //数値チェック
            if (in_array($key, array("SCHREGNO",  "CLASSCD", "CURRICULUM_CD", "SUBCLASSCD"))) {
                if ($data[$key] != "" && !is_numeric($data[$key])) {
                    $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, $val[1]);
                }
            }

            //所見文字数チェック
            if ($val[0] == "LENGTH") {
                $moji = $val[2];
                $gyou = $val[3];
                if ($this->validate_row_cnt($data[$key], ($moji * 2)) > $gyou) {
                    $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※文字数", $lengthSep, $val[1]);
                }
            }
        }

        if (!$returnFlg) {
            $checkError = $duplicateCsvErrMsg.$hissuErrMsg.$numericErrMsg.$lengthErrMsg;
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
        $header["CLASSCD"]          = "※教科コード";
        $header["SCHOOL_KIND"]      = "※学校校種";
        $header["CURRICULUM_CD"]    = "※教育課程コード";
        $header["SUBCLASSCD"]       = "※科目コード";
        foreach ($this->itemMst as $m) {
            $header[$m["COLUMNNAME"]]    = $m["ITEMNAME"];
        }
        $header[$this->lastColumn]  = $this->lastColumn;

        $contents  = "";    //クリア

        $contents .= implode($header, ",")."\n";

        //DB接続
        $db = Query::dbCheckOut();

        //データ
        if ($this->Properties["KNJD133C_semesCombo"] == "1") {
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
        $query = knjd133cQuery::selectQuery($this, $execute_date);
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
            $setData = array();
            $setData["HR_NAME"]     = $row["HR_NAME"];
            $setData["ATTENDNO"]    = $row["ATTENDNO"];
            $setData["NAME_SHOW"]   = $row["NAME_SHOW"];
            $setData["SCHREGNO"]    = $row["SCHREGNO"];
            //教育課程対応
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
            foreach ($this->itemMst as $m) {
                $setData[$m["COLUMNNAME"]] = $row[$m["COLUMNNAME"]];
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
