<?php

require_once('for_php7.php');
class knjd133hModel extends Model
{
    public $field = array();
    public $data  = array();
    public $cmd;
    public $chaircd = "";
    public $file;
    public $replace_get = array();
    public $lastColumn;//CSVの最後の列の値

    public function knjd133hModel()
    {
    }

    public function init()
    {
        if (VARS::request("PROGRAMID")) {
            $this->field = array();
            common::GetControlMaster_Fnc($this->control);
        }

        //学期
        $this->field["SEMESTER"] = VARS::post("SEMESTER");

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

        if (VARS::get("SUBCLASSCD")) {
            $this->subclasscd = VARS::get("SUBCLASSCD");
        }
        if (VARS::get("CHAIRCD")) {
            $this->chaircd = VARS::get("CHAIRCD");
        }
        //一括更新画面
        if (VARS::get("SEMESTER")) {
            $this->replace_get['SEMESTER'] = VARS::get("SEMESTER");
        }
        if (VARS::get("SUBCLASSCD")) {
            $this->replace_get['SUBCLASSCD'] = VARS::get("SUBCLASSCD");
        }
        if (VARS::get("CHAIRCD")) {
            $this->replace_get['CHAIRCD'] = VARS::get("CHAIRCD");
        }
        if (VARS::get("GRADE")) {
            $this->replace_get['GRADE'] = VARS::get("GRADE");
        }

        //定型文選択
        if (VARS::get("SEM")) {
            $this->sem = VARS::get("SEM");
        }
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
        $this->replace_data  = array();
        for ($i = 0; $i < get_count($this->data["SCHREGNO"]); $i++) {
            $totalstudyact      = "TOTALSTUDYACT-".$i;
            $totalstudytime     = "TOTALSTUDYTIME-".$i;
            $grad_value         = "GRAD_VALUE-".$i;
            $comp_credit        = "COMP_CREDIT-".$i;
            $get_credit         = "GET_CREDIT-".$i;
            $chk_calc_credit    = "CHK_CALC_CREDIT-".$i;
            $prov_flg           = "PROV_FLG-".$i;
            $this->fields["TOTALSTUDYACT"][]    = VARS::post($totalstudyact);
            $this->fields["TOTALSTUDYTIME"][]   = VARS::post($totalstudytime);
            $this->fields["GRAD_VALUE"][]       = VARS::post($grad_value);
            $this->fields["COMP_CREDIT"][]      = VARS::post($comp_credit);
            $this->fields["GET_CREDIT"][]       = VARS::post($get_credit);
            $this->fields["CHK_CALC_CREDIT"][]  = VARS::post($chk_calc_credit);
            $this->fields["PROV_FLG"][]         = VARS::post($prov_flg);
        }

        //サイズ初期値
        if ($this->Properties["RECORD_TOTALSTUDYTIME_DAT_TOTALSTUDYACT_SIZE_H"]) {
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["RECORD_TOTALSTUDYTIME_DAT_TOTALSTUDYACT_SIZE_H"]);
            $this->getPro["TOTALSTUDYACT"]["moji"] = (int)trim($moji);
            $this->getPro["TOTALSTUDYACT"]["gyou"] = (int)trim($gyou);
        } else {
            $this->getPro["TOTALSTUDYACT"]["moji"] = 22;
            $this->getPro["TOTALSTUDYACT"]["gyou"] = 6;
        }
        if ($this->Properties["RECORD_TOTALSTUDYTIME_DAT_TOTALSTUDYTIME_SIZE_H"]) {//評価
            list($moji, $gyou) = preg_split("/\*/", $this->Properties["RECORD_TOTALSTUDYTIME_DAT_TOTALSTUDYTIME_SIZE_H"]);
            $this->getPro2["TOTALSTUDYTIME"]["moji"] = (int)trim($moji);
            $this->getPro2["TOTALSTUDYTIME"]["gyou"] = (int)trim($gyou);
        } else {
            $this->getPro2["TOTALSTUDYTIME"]["moji"] = 22;
            $this->getPro2["TOTALSTUDYTIME"]["gyou"] = 6;
        }

        //学校判定
        $db = Query::dbCheckOut();
        $this->isBunkyo = $db->getOne(knjd133hQuery::getSchoolHantei("bunkyo"));
        Query::dbCheckIn($db);
        
        /*********************/
        /* 一括更新処理処理1 */
        /*********************/
        if ($this->cmd == 'replace_update1') {
            $this->replace_data  = array();
            $this->replace_data["selectdata"]  = VARS::post("selectdata");
            $this->replace_data["check"]       = VARS::post("CHECK");
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
            if ($this->validate_row_cnt($this->fields["TOTALSTUDYACT"][$i], ($this->getPro["TOTALSTUDYACT"]["moji"] * 2)) > $this->getPro["TOTALSTUDYACT"]["gyou"]) {
                $this->setWarning("MSG915", "( 学習内容は{$this->getPro["TOTALSTUDYACT"]["gyou"]}行までです )");
                return false;
            }
            if ($this->validate_row_cnt($this->fields["TOTALSTUDYTIME"][$i], ($this->getPro2["TOTALSTUDYTIME"]["moji"] * 2)) > $this->getPro2["TOTALSTUDYTIME"]["gyou"]) {
                $this->setWarning("MSG915", "( 評価は{$this->getPro2["TOTALSTUDYTIME"]["gyou"]}行までです )");
                return false;
            }
        }
        return true;
    }

    public function &getUpdateModel()
    {
        if ($this->validateInput()) {
            knjd133hQuery::update($this);
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
            $tables = array("1" => "RECORD_TOTALSTUDYTIME_DAT", "2" => "RECORD_SCORE_DAT", "3" => "RECORD_PROV_FLG_DAT");
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
            $checkdata["SCHREGNO"]          = array(8,"学籍番号");
            if ($this->Properties["useCurriculumcd"] == '1') {
                $checkdata["CLASSCD"]           = array(2,"教科コード");
                $checkdata["SCHOOL_KIND"]       = array(2,"学校校種");
                $checkdata["CURRICULUM_CD"]     = array(2,"教育課程コード");
            }
            $checkdata["SUBCLASSCD"]        = array(6,"科目コード");
            if ($this->Properties["useOnlyTotalstudyTime"] == '1') {
                $checkdata["TOTALSTUDYTIME"]    = array(500,"評価");
            } else {
                $checkdata["TOTALSTUDYACT"]     = array(678,"学習内容");
                if ($this->Properties["useTotalstudyTime"] == '1') {
                    $checkdata["TOTALSTUDYTIME"]    = array(500,"評価");
                }
            }
            if ($this->Properties["useKnjd133hOnlyGradValue"] == '2') {
            } elseif ($this->Properties["useKnjd133hOnlyGradValue"] == '1') {
                $checkdata["GRAD_VALUE"]        = array(2,"学年評定");
            } else {
                $checkdata["COMP_CREDIT"]       = array(2,"履修単位");
                $checkdata["GET_CREDIT"]        = array(2,"修得単位");
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
            $primaryKey2 = array();
            $duplicateCsv2 = array();
            $primaryKey3 = array();
            $duplicateCsv3 = array();
            $primaryKey1 = $this->getPrimaryKey('RECORD_TOTALSTUDYTIME_DAT');
            $primaryKey2 = $this->getPrimaryKey('RECORD_SCORE_DAT');
            $primaryKey3 = $this->getPrimaryKey('RECORD_PROV_FLG_DAT');

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

                    $tmpCnt = 3;
                    $data = array();
                    $data["SCHREGNO"]           = (strlen(trim($tmp[$tmpCnt]))) ? sprintf("%08d", $tmp[$tmpCnt]) : "";
                    if ($this->Properties["useCurriculumcd"] == '1') {
                        $data["CLASSCD"]        = $tmp[++$tmpCnt];
                        $data["SCHOOL_KIND"]    = $tmp[++$tmpCnt];
                        $data["CURRICULUM_CD"]  = $tmp[++$tmpCnt];
                    }
                    $data["SUBCLASSCD"]         = $tmp[++$tmpCnt];
                    if ($this->Properties["useOnlyTotalstudyTime"] == '1') {
                        $data["TOTALSTUDYTIME"] = $tmp[++$tmpCnt];
                    } else {
                        $data["TOTALSTUDYACT"]      = $tmp[++$tmpCnt];
                        if ($this->Properties["useTotalstudyTime"] == '1') {
                            $data["TOTALSTUDYTIME"] = $tmp[++$tmpCnt];
                        }
                    }
                    if ($this->Properties["useKnjd133hOnlyGradValue"] == '2') {
                    } elseif ($this->Properties["useKnjd133hOnlyGradValue"] == '1') {
                        $data["GRAD_VALUE"]     = $tmp[++$tmpCnt];
                    } else {
                        $data["COMP_CREDIT"]    = $tmp[++$tmpCnt];
                        $data["GET_CREDIT"]     = $tmp[++$tmpCnt];
                    }
                    $data["YEAR"]           = CTRL_YEAR;
                    $data["SEMESTER"]       = $this->semester;
                    $data["TESTKINDCD"]     = "99";
                    $data["TESTITEMCD"]     = "00";
                    $data["SCORE_DIV"]      = ($this->Properties["useTestCountflg"] == 'TESTITEM_MST_COUNTFLG_NEW_SDIV') ? '09' : '00';

                    //エラーチェック
                    if ($this->errCheck($db, $data, $checkdata, $line, $duplicateCsv1, $duplicateCsv2, $duplicateCsv3, $primaryKey1, $primaryKey2, $primaryKey3)) {
                        $temp_arr[$data["SCHREGNO"]] = $data;
                        $i++;
                    }
                    foreach ($primaryKey1 as $key => $val) {
                        $duplicateCsv1[$line][$val] = $data[$val];
                    }
                    foreach ($primaryKey2 as $key => $val) {
                        $duplicateCsv2[$line][$val] = $data[$val];
                    }
                    foreach ($primaryKey3 as $key => $val) {
                        $duplicateCsv3[$line][$val] = $data[$val];
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
    public function errCheck($db, $data, $data_each, $line, $duplicateCsv1, $duplicateCsv2, $duplicateCsv3, $primaryKey1, $primaryKey2, $primaryKey3)
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

        $dupFlg2 = $this->duplicateCsvCheck($duplicateCsv2, $primaryKey2, $data);
        if ($dupFlg2[0] == 1) {
            $returnFlg = $this->errSet($duplicateCsvErrFlg, $duplicateCsvErrMsg, "※重複(CSV):", $duplicateCsvSep, "重複2(". $dupFlg2[1] ."行目)");
        }

        $dupFlg3 = $this->duplicateCsvCheck($duplicateCsv3, $primaryKey3, $data);
        if ($dupFlg3[0] == 1) {
            $returnFlg = $this->errSet($duplicateCsvErrFlg, $duplicateCsvErrMsg, "※重複(CSV):", $duplicateCsvSep, "重複3(". $dupFlg3[1] ."行目)");
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
        $header = array();
        $header["HR_NAME"]              = "クラス";
        $header["ATTENDNO"]             = "出席番号";
        $header["NAME_SHOW"]            = "氏名";
        $header["SCHREGNO"]             = "※学籍番号";
        if ($this->Properties["useCurriculumcd"] == '1') {
            $header["CLASSCD"]          = "※教科コード";
            $header["SCHOOL_KIND"]      = "※学校校種";
            $header["CURRICULUM_CD"]    = "※教育課程コード";
        }
        $header["SUBCLASSCD"]           = "※科目コード";
        if ($this->Properties["useOnlyTotalstudyTime"] == '1') {
            $header["TOTALSTUDYTIME"]   = "評価";
        } else {
            $header["TOTALSTUDYACT"]        = "学習内容";
            if ($this->Properties["useTotalstudyTime"] == '1') {
                $header["TOTALSTUDYTIME"]   = "評価";
            }
        }
        if ($this->Properties["useKnjd133hOnlyGradValue"] == '2') {
        } elseif ($this->Properties["useKnjd133hOnlyGradValue"] == '1') {
            $header["GRAD_VALUE"]       = "学年評定";
        } else {
            $header["COMP_CREDIT"]      = "履修単位";
            $header["GET_CREDIT"]       = "修得単位";
        }
        $header[$this->lastColumn]      = $this->lastColumn;

        $contents  = "";    //クリア

        $contents .= implode($header, ",")."\n";

        //DB接続
        $db = Query::dbCheckOut();

        //データ
        $sdate = str_replace("/", "-", $this->control["学期開始日付"][CTRL_SEMESTER]);
        $edate = str_replace("/", "-", $this->control["学期終了日付"][CTRL_SEMESTER]);
        if ($sdate <= CTRL_DATE && CTRL_DATE <= $edate) {
            $execute_date = CTRL_DATE;  //初期値
        } else {
            $execute_date = $edate;     //初期値
        }
        $query = knjd133hQuery::selectQuery($this, $execute_date, $this->field["SEMESTER"], $this->field["SUBCLASSCD"], $this->field["CHAIRCD"]);
        //SUBCLASSCDセット
        //教育課程対応
        if ($this->Properties["useCurriculumcd"] == '1') {
            list($CLASSCD, $SCHOOL_KIND, $CURRICULUM_CD, $SUBCLASSCD) = explode("-", $this->field["SUBCLASSCD"]);
        }

        $header_data = false;

        //SQL文発行
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $row = str_replace(",", ".", $row);  //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
            $row = str_replace("\r\n", "↓", $row);
            $row = str_replace("\r", "↓", $row);
            $row = str_replace("\n", "↓", $row);
            $row[$this->lastColumn] = $this->lastColumn;

            $sep = "";
            foreach ($header as $key => $val) {
                //教育課程対応
                if ($this->Properties["useCurriculumcd"] == '1') {
                    if (in_array($key, array("CLASSCD", "SCHOOL_KIND", "CURRICULUM_CD", "SUBCLASSCD")) && !$row[$key]) {
                        $contents .= $sep.$$key;
                    } else {
                        $contents .= $sep.$row[$key];
                    }
                } else {
                    if ($key == "SUBCLASSCD" && !$row[$key]) {
                        $contents .= $sep.$this->field["SUBCLASSCD"];
                    } else {
                        $contents .= $sep.$row[$key];
                    }
                }
                $sep = ",";
            }
            $contents .= "\n";
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

    /*****************/
    /* 一括更新処理1 */
    /*****************/
    public function &replaceModel()
    {
        //生徒が選択されてない場合
        if (!strlen($this->replace_data["selectdata"])) {
            $this->setWarning("MSG304");
            return false;
        }
        
        $totalstudytime = '';
        foreach ($this->replace_data["check"] as $key => $value) {
            $totalstudytime .= $sep . $value;
            $sep = "\n";
        }
            
        if ($this->validate_row_cnt($totalstudytime, ($this->getPro2["TOTALSTUDYTIME"]["moji"] * 2)) > $this->getPro2["TOTALSTUDYTIME"]["gyou"]) {
            $this->setWarning("MSG915", "( 評価は{$this->getPro2["TOTALSTUDYTIME"]["gyou"]}行までです )");
            return false;
        }
        knjd133hQuery::getReplaceQuery($this, $totalstudytime);
        $this->setMessage("MSG201");
    }
}
