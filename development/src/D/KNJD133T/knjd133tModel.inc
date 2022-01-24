<?php

require_once('for_php7.php');
class knjd133tModel extends Model
{
    public $field = array();
    public $data  = array();
    public $cmd;
    public $replace_data = array();    //置換で選択された生徒一覧
    public $chaircd = "";
    public $file;
    public $itemMst = array();
    public $lastColumn;//CSVの最後の列の値

    public function knjd133tModel()
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

        //科目CD(探求)
        $this->commentTitle = (substr($this->field["SUBCLASSCD"], 0, 2) == "22") ? "態度・行動に関する所見" : "コメント";

        //コメント
        $this->moji = "50";
        $this->gyou = "10";

        //初期化
        $this->fields = array();
        if (is_array($this->data["SCHREGNO"])) {
            foreach ($this->data["SCHREGNO"] as $schregno) {
                $commentKey     = "COMMENT_".$schregno;
                $this->fields[$commentKey]      = VARS::post($commentKey);
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
        foreach ($this->data["SCHREGNO"] as $schregno) {
            $commentKey     = "COMMENT_".$schregno;
            $attendNameKey  = "ATTENDO_NAME_".$schregno;
            $this->fields[$attendNameKey]   = VARS::post($attendNameKey);

            if ($this->validate_row_cnt($this->fields[$commentKey], ($this->moji * 2)) > $this->gyou) {
                $this->setWarning("MSG915", "「{$this->commentTitle}」は{$this->gyou}行までです。 ({$this->fields[$attendNameKey]})");
                return false;
            }
        }
        return true;
    }

    public function &getUpdateModel()
    {
        if ($this->validateInput()) {
            knjd133tQuery::update($this);
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
            $tables = array("1" => "PYP_COMMENT_DAT");
            $header = array("YEAR"              => "年度",
                            "SEMESTER"          => "学期",
                            "CLASSCD"           => "教科コード",
                            "SCHOOL_KIND"       => "学校校種",
                            "CURRICULUM_CD"     => "教育課程コード",
                            "SUBCLASSCD"        => "科目コード",
                            "SCHREGNO"          => "学籍番号");
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
            $checkdata = array();
            $checkdata["YEAR"]          = array(4,          "年度");
            $checkdata["SEMESTER"]      = array(1,          "学期");
            $checkdata["SUBCLASSCD"]    = array(6,          "科目コード");
            $checkdata["SCHREGNO"]      = array(8,          "学籍番号");
            $checkdata["COMMENT"]       = array("LENGTH",   "{$this->commentTitle}", $this->moji, $this->gyou);

            $db = Query::dbCheckOut();
            $db->autoCommit(false);

            $fp = fopen($this->file["tmp_name"], "r");
            $i  = 0;
            $line = 1;  //行数
            $header = false;
            $primaryKey1 = array();
            $primaryKey1 = $this->getPrimaryKey('PYP_COMMENT_DAT');
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
                    $tmp    = str_replace("↓", "\n", $tmp);

                    $data = array();
                    $data["YEAR"]          = $tmp[0];
                    $data["SEMESTER"]      = $tmp[1];
                    $data["SUBCLASSCD"]    = $tmp[2];
                    $data["SCHREGNO"]      = (strlen(trim($tmp[4]))) ? sprintf("%08s", $tmp[4]) : "";
                    $data["COMMENT"]       = $tmp[6];

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
        $hissuErrFlg        = false;    //必須エラーフラグ
        $numericErrFlg      = false;    //数字エラーフラグ
        $lengthErrFlg       = false;    //レングスエラーフラグ
        $huittiErrFlg       = false;    //不一致エラーフラグ
        $existsErrFlg       = false;    //存在エラーフラグ
        $duplicateCsvErrFlg = false;    //重複エラーフラグ

        $hissuErrMsg        = "";       //必須エラーメッセージ
        $numericErrMsg      = "";       //数字エラーメッセージ
        $lengthErrMsg       = "";       //レングスエラーメッセージ
        $huittiErrMsg       = "";       //不一致エラーメッセージ
        $existsErrMsg       = "";       //存在エラーメッセージ
        $duplicateCsvErrMsg = "";       //重複エラーメッセージ

        $hissuSep           = "";
        $numericSep         = "";
        $lengthSep          = "";
        $huittiSep          = "";
        $existsSep          = "";
        $duplicateCsvSep    = "";

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

            //コンボで指定した値と一致するかチェック
            if ($key == "SEMESTER") {
                if ($this->semester != $data[$key]) {
                    $returnFlg = $this->errSet($huittiErrFlg, $huittiErrMsg, "※不一致:", $huittiSep, $val[1]);
                }
            } elseif ($key == "SUBCLASSCD") {
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
            if (in_array($key, array("YEAR",  "SEMESTER", "SUBCLASSCD", "SCHREGNO"))) {
                if ($data[$key] != "" && !is_numeric($data[$key])) {
                    $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, $val[1]);
                }
            }

            //所見文字数チェック
            if ($val[0] == "LENGTH") {
                $moji = $val[2];
                $gyou = $val[3];
                if ($this->validate_row_cnt($data[$key], ($moji * 2)) > $gyou) {
                    $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※文字数:", $lengthSep, $val[1]);
                }
            } else {
                //文字数チェック
                if (strlen($data[$key]) > $val[0]) {
                    $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[1]);
                }
            }
        }
        if (!$returnFlg) {
            $checkError = $duplicateCsvErrMsg.$hissuErrMsg.$existsErrMsg.$numericErrMsg.$lengthErrMsg.$huittiErrMsg;
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
        $header["COMMENT"]         = "{$this->commentTitle}";
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
        $query = knjd133tQuery::selectQuery($this, $execute_date);
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
            $setData["YEAR"]            = CTRL_YEAR;
            $setData["SEMESTER"]        = $setSemster;
            $setData["SUBCLASSCD"]      = $substrcd[3];
            $setData["SUBCLASSNAME"]    = $row["SUBCLASSNAME"];
            $setData["SCHREGNO"]        = $row["SCHREGNO"];
            $setData["NAME_SHOW"]       = $row["NAME_SHOW"];
            $setData["COMMENT"]         = $row["COMMENT1"];
            $setData[$this->lastColumn] = $this->lastColumn;
            $contents .= implode($setData, ",") ."\n";
            $header_data = true;
        }
        $result->free();
        Query::dbCheckIn($db);

        //ＣＳＶ出力
        if ($header_data) {
            $couse = "PYP通知票コメント入力";

            $filename = sprintf("%s.csv", $couse);
            common::downloadContents($contents, $filename, "SJIS-win");
            return true;
        } else {
            $this->warning = "該当データは、ありません。";
            return false;
        }
    }
}
