<?php

require_once('for_php7.php');

class knjd133kModel extends Model
{
    public $field = array();
    public $data  = array();
    public $cmd;
    public $replace_data   = array();    //置換で選択された生徒一覧
    public $chaircd = "";
    public $file;
    public $lastColumn;//CSVの最後の列の値

    /**
     * メンバ変数の初期化
     */
    public function knjd133kModel()
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

        if (VARS::get("SUBCLASSCD")) {
            $this->subclasscd = VARS::get("SUBCLASSCD");
        }
        if (VARS::get("CHAIRCD")) {
            $this->chaircd = VARS::get("CHAIRCD");
        }

        if ($this->cmd == "back") {
            unset($this->replace_data);
        }

        //プロパティファイルの読込み
        $this->getPropertiesAll();

        $this->totalstudyact_moji = 11; //デフォルトの値
        $this->totalstudyact_gyou = 4;  //デフォルトの値
        $this->totalstudytime_moji = 11; //デフォルトの値
        $this->totalstudytime_gyou = 6;  //デフォルトの値

        $this->file = VARS::file("FILE");

        $this->lastColumn = 'DUMMY';
        if ($this->Properties["csv_LastColumn"] != '') {
            $this->lastColumn = $this->Properties["csv_LastColumn"];
        }
    }

    //入力項目チェック
    public function validateInput()
    {
        for ($i = 0; $i < get_count($this->data["SCHREGNO"]); $i++) {
            if ($this->validate_row_cnt($this->fields["TOTALSTUDYACT"][$i], ($this->totalstudyact_moji * 2)) > $this->totalstudyact_gyou) {
                if ($this->validate_row_cnt($this->fields["TOTALSTUDYTIME"][$i], ($this->totalstudytime_moji * 2)) > $this->totalstudytime_gyou) {
                    $this->setWarning("MSG915", "(学習内容は{$this->totalstudyact_gyou}行まで、評価は{$this->totalstudytime_gyou}行までです )");
                    return false;
                } else {
                    $this->setWarning("MSG915", "(学習内容は{$this->totalstudyact_gyou}行までです )");
                    return false;
                }
            }
            if ($this->validate_row_cnt($this->fields["TOTALSTUDYTIME"][$i], ($this->totalstudytime_moji * 2)) > $this->totalstudytime_gyou) {
                $this->setWarning("MSG915", "(評価は{$this->totalstudytime_gyou}行までです )");
                return false;
            }
        }
        return true;
    }

    public function &getUpdateModel()
    {
        //初期化
        $this->fields = array();
        for ($i = 0; $i < get_count($this->data["SCHREGNO"]); $i++) {
            $totalstudyact      = "TOTALSTUDYACT-".$i;
            $totalstudytime     = "TOTALSTUDYTIME-".$i;
            $chk_calc_credit    = "CHK_CALC_CREDIT-".$i;
            $this->fields["TOTALSTUDYACT"][]    = VARS::post($totalstudyact);
            $this->fields["TOTALSTUDYTIME"][]   = VARS::post($totalstudytime);
            $this->fields["CHK_CALC_CREDIT"][]  = VARS::post($chk_calc_credit);
        }

        if ($this->validateInput()) {
            knjd133kQuery::update($this);
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
            $tables = array("1" => "RECORD_TOTALSTUDYTIME_DAT",
                            "2" => "KIN_RECORD_DAT"
                            );
            $header = array("YEAR"              => "年度",
                            "SEMESTER"          => "学期",
                            "SCHREGNO"          => "学籍番号",
                            "CLASSCD"           => "教科コード",
                            "SCHOOL_KIND"       => "学校校種",
                            "CURRICULUM_CD"     => "教育課程コード",
                            "SUBCLASSCD"        => "科目コード");
            $dupTable = $this->getDuplicateErrorHeaderNoSave(PROGRAMID, $tables, $this->errData);
            if (sizeof($dupTable) > 0) {
                $headerErr = $this->duplicateCsvHeader($dupTable, $header);
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
                                   "TOTALSTUDYTIME" => array(500,"評価"));
            } else {
                $checkdata = array("SCHREGNO"       => array(8,"学籍番号"),
                                   "SUBCLASSCD"     => array(6,"科目コード"),
                                   "TOTALSTUDYACT"  => array(678,"学習内容"),
                                   "TOTALSTUDYTIME" => array(500,"評価"));
            }

            $db = Query::dbCheckOut();
            $db->autoCommit(false);

            $fp = fopen($this->file["tmp_name"], "r");
            $i  = 0;
            $line = 1;  //行数
            $header = false;
            $primaryKey1 = array();
            $primaryKey1 = $this->getPrimaryKey('RECORD_TOTALSTUDYTIME_DAT');
            $duplicateCsv1 = array();

            $primaryKey2 = array();
            $primaryKey2 = $this->getPrimaryKey('KIN_RECORD_DAT');
            $duplicateCsv2 = array();

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
                                      "TOTALSTUDYTIME"  => $tmp[9]);
                    } else {
                        $data = array("SCHREGNO"        => (strlen(trim($tmp[3]))) ? sprintf("%08d", $tmp[3]) : "",
                                      "SUBCLASSCD"      => $tmp[4],
                                      "TOTALSTUDYACT"   => $tmp[5],
                                      "TOTALSTUDYTIME"  => $tmp[6]);
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
        $returnFlg          = true;
        $hissuErrFlg        = false;    //必須エラーフラグ
        $numericErrFlg      = false;    //数字エラーフラグ
        $duplicateCsvErrFlg = false;    //重複エラーフラグ

        $hissuErrMsg        = "";       //必須エラーメッセージ
        $numericErrMsg      = "";       //数字エラーメッセージ
        $duplicateCsvErrMsg = "";       //重複エラーメッセージ

        $hissuSep           = "";
        $numericSep         = "";
        $duplicateCsvSep    = "";

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
        //教育課程対応
        if ($this->Properties["useCurriculumcd"] == '1') {
            $subclass_array = array();
            $subclass_array = explode("-", $this->field["SUBCLASSCD"]);
        }
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
                            $this->lastColumn   => $this->lastColumn);
        } else {
            $header = array("HR_NAME"           => "クラス",
                            "ATTENDNO"          => "出席番号",
                            "NAME_SHOW"         => "氏名",
                            "SCHREGNO"          => "※学籍番号",
                            "SUBCLASSCD"        => "※科目コード",
                            "TOTALSTUDYACT"     => "学習内容",
                            "TOTALSTUDYTIME"    => "評価",
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
        $query = knjd133kQuery::selectQuery($this, $execute_date);

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
                $setData["CLASSCD"]         = $subclass_array[0];
                $setData["SCHOOL_KIND"]     = $subclass_array[1];
                $setData["CURRICULUM_CD"]   = $subclass_array[2];
                $setData["SUBCLASSCD"]      = $subclass_array[3];
            } else {
                $setData["SUBCLASSCD"]      = $this->field["SUBCLASSCD"];
            }
            $setData["TOTALSTUDYACT"] = $row["TOTALSTUDYACT"];
            $setData["TOTALSTUDYTIME"] = $row["TOTALSTUDYTIME"];
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
