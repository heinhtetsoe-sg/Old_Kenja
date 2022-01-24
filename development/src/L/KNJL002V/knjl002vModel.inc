<?php
class knjl002vModel extends Model
{
    public $field = array();
    public $cmd;
    public $examyear;
    public $leftYear = "";
    public $schoolKind = "";
    public $examId = "";
    public $examSubclass;
    public $maxLevel;
    public $mantendiv;
    public $mantenMark;
    public $perfectField = array();
    public $dataPerfectField = array();

    public function knjl002vModel()
    {
    }

    public function init()
    {
        //起動時に初期化
        if (VARS::get("PROGRAMID")) {
            unset($this->leftYear);
            unset($this->schoolKind);
            unset($this->examId);
            unset($this->applicantDiv);
            unset($this->courseDiv);
            unset($this->frequency);
            unset($this->examSubclass);
            unset($this->maxLevel);
            unset($this->mantendiv);
        }

        //プロパティファイル
        $this->getPropertiesAll();

        $this->cmd = VARS::request("cmd");

        $this->examyear  = CTRL_YEAR + 1;

        $this->field = array("APPLICANT_DIV"   => VARS::post("APPLICANT_DIV"),
                             "COURSE_DIV"      => VARS::post("COURSE_DIV"),
                             "FREQUENCY"       => VARS::post("FREQUENCY"),
                             "EXAM_NAME"       => VARS::post("EXAM_NAME"),
                             "EXAM_DATE"       => VARS::post("EXAM_DATE"),
                             "AM_PM"           => VARS::post("AM_PM"),
                             "EXAM_NO_BAND"    => VARS::post("EXAM_NO_BAND"),
                             "EXAM_SUBCLASS"   => VARS::post("EXAM_SUBCLASS")
                           );

        $this->perfectField = array();

        //左画面 年度コンボ
        if (VARS::request("YEAR") != "") {
            $this->leftYear = VARS::request("YEAR");
        }
        $this->leftYear = ($this->leftYear == "") ? $this->examyear : $this->leftYear;

        if (VARS::request("EXAM_SCHOOL_KIND") != "") {
            $this->schoolKind = VARS::request("EXAM_SCHOOL_KIND");
        }

        if (VARS::get("EXAM_ID") != "") {
            $this->examId = VARS::get("EXAM_ID");
            $this->applicantDiv = substr($this->examId, 1, 2);
            $this->courseDiv    = substr($this->examId, 3, 4);
            $this->frequency    = substr($this->examId, 7, 2);
        }

        if (VARS::get("ini2")) {
            unset($this->dataPerfectField);
        }

        //科目 満点マスタ
        $this->examSubclass  = VARS::post("EXAM_SUBCLASS");
        //評価段階数  満点マスタ
        $this->maxLevel  = VARS::post("MAX_POINTLEVEL");
        //満点/評価 満点マスタ
        $this->mantendiv  = VARS::post("MANTENDIV");
        $this->mantenMark = "A";
    }

    //コピーのモデルを返します
    public function &getCopyModel()
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $message = '';
        $flg = true;

        $query = knjl002vQuery::getCheckEntExamStdMstSql($this->leftYear);
        $result = $db->getOne($query);
        if ($result > 0) {
            $message = '今年度のデータが存在します。';
            $flg = false;
        }

        $prevYear = $this->leftYear - 1;
        $query = knjl002vQuery::getCheckEntExamStdMstSql($prevYear);
        $result = $db->getOne($query);
        if ($result <= 0) {
            $message = '前年データが存在しません。';
            $flg = false;
        }

        if ($flg) {
            $query = knjl002vQuery::getCopyQuery($this, $prevYear);
            $db->query($query);

            $query = knjl002vQuery::getCopyPerfectMstQuery($this, $prevYear);
            $db->query($query);

            $this->setMessage("MSG201");
        } else {
            $this->setMessage("MSG203", "\\n" . $message);
        }

        $db->commit();
        Query::dbCheckIn($db);
    }

    //入力項目チェック
    public function validateInput()
    {
        //キー変更チェック
        if ($this->cmd == "update" || $this->cmd == "delete") {
            if ($this->field["APPLICANT_DIV"]  != $this->applicantDiv ||
                $this->field["COURSE_DIV"]     != $this->courseDiv ||
                $this->field["FREQUENCY"]      != $this->frequency) {
                $this->setWarning("MSG308");
                return false;
            }
        }

        //必須チェック
        if ($this->cmd == "add" || $this->cmd == "update" || $this->cmd == "delete") {
            if ($this->field["APPLICANT_DIV"] == "") {
                $this->setWarning("MSG301", "( 入試コード )");
                return false;
            }
            if ($this->field["COURSE_DIV"] == "") {
                $this->setWarning("MSG301", "( 志望コース )");
                return false;
            }
            if ($this->field["FREQUENCY"] == "") {
                $this->setWarning("MSG301", "( 回数 )");
                return false;
            }
        }
        if ($this->cmd == "add" || $this->cmd == "update") {
        }

        //コードのゼロ埋め
        $this->field["FREQUENCY"] = sprintf("%02d", $this->field["FREQUENCY"]);
        if ($this->field["EXAM_NO_BAND"] != "") {
            $this->field["EXAM_NO_BAND"] = sprintf("%04d", $this->field["EXAM_NO_BAND"]);
        }

        //１レコード取得
        $query = knjl002vQuery::getRow($this, "chk");
        $db = Query::dbCheckOut(); //DB接続
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);     //DB切断
        if (($this->cmd == "update" || $this->cmd == "delete") && !is_array($row)) {
            $this->setWarning("MSG303");
            return false;
        }

        //重複チェック
        if ($this->cmd == "add" && is_array($row)) {
            $this->setWarning("MSG918");
            return false;
        }

        //文字数チェック
        $checkStrLen["FREQUENCY"]  = array("LEN" => 2, "MESAGE" => "回数");
        $checkStrLen["EXAM_NAME"]  = array("LEN" => 60, "MESAGE" => "試験名");
        $checkStrLen["EXAM_NO_BAND"]  = array("LEN" => 4, "MESAGE" => "受験番号開始番号");
        foreach ($checkStrLen as $key => $val) {
            if (strlen($this->field[$key]) > $val["LEN"]) {
                $this->setWarning("MSG915", $val["MESAGE"]." ".$val["LEN"]."バイトまで");
                return false;
            }
        }
        //試験日の日付型チェック
        if ($this->field["EXAM_DATE"] != "") {
            $matches = array();
            if (preg_match("/([0-9]{4})[\/]([0-9]{1,2})[\/]([0-9]{1,2})/", $this->field["EXAM_DATE"], $matches) != 1) {
                $this->setWarning("MSG902", "試験日");
                return false;
            } else {
                if (!checkdate($matches[2], $matches[3], $matches[1])) {
                    $this->setWarning("MSG902", "試験日");
                    return false;
                }
            }
        }

        return true;
    }

    //編集完了用のモデルを返します
    public function &getUpdateModel()
    {
        //入力チェック
        if ($this->validateInput()) {
            $this->update();
            $this->examId = $this->schoolKind.$this->field["APPLICANT_DIV"].$this->field["COURSE_DIV"].$this->field["FREQUENCY"];
            $this->setMessage("MSG201");
        }
    }

    //アップデート時の内部メソッド
    public function &update()
    {
        $result = knjl002vQuery::getUpdateQuery($this, $this->field);
        return $result;
    }

    public function &getInsertModel()
    {
        //入力チェック
        if ($this->validateInput()) {
            $this->insert();
            $this->examId = $this->schoolKind.$this->field["APPLICANT_DIV"].$this->field["COURSE_DIV"].$this->field["FREQUENCY"];
            $this->setMessage("MSG201");
            $this->applicantDiv = $this->field["APPLICANT_DIV"];
            $this->courseDiv    = $this->field["COURSE_DIV"];
            $this->frequency    = $this->field["FREQUENCY"];
        }
        return;
    }

    //インサート
    public function &insert()
    {
        $result = knjl002vQuery::getInsertQuery($this, $this->field);
        return $result;
    }

    //DELETE
    public function &getDeleteModel()
    {
        if ($this->validateInput()) {
            knjl002vQuery::getDeleteQuery($this, $this->field);
            $this->examId = "";
            $this->setMessage("MSG201");
            $this->field = array();
        }
    }

    //入力項目チェック
    public function validateInputPerfect()
    {
        if ($this->mantendiv == "1") {
            $cnt = 1;
        } else {
            $cnt = $this->maxLevel;
        }

        //文字数チェック
        $checkStrLen["LABEL"]       = array("LEN" => 2,  "MESAGE" => "表示");
        $checkStrLen["VALUE"]       = array("LEN" => 3,  "MESAGE" => "値");

        for ($i = 1; $i <= $cnt; $i++) {
            //必須チェック
            if ($this->dataPerfectField[$i]["LABEL"] == '') {
                $this->setWarning("MSG301", "( 表示 )");
                return false;
            }
            if ($this->dataPerfectField[$i]["VALUE"] == '') {
                $this->setWarning("MSG301", "( 値 )");
                return false;
            }

            //文字数チェック
            foreach ($checkStrLen as $key => $val) {
                if (strlen($this->dataPerfectField[$i][$key]) > $val["LEN"]) {
                    $this->setWarning("MSG915", $val["MESAGE"]." ".$val["LEN"]."バイトまで");
                    return false;
                }
            }
        }

        return true;
    }

    //満点マスタ 編集完了用のモデルを返します
    public function &getUpdatePerfectModel()
    {
        if ($this->mantendiv == "1") {
            $cnt = 1;
        } else {
            $cnt = $this->maxLevel;
        }

        //入力値取得
        for ($i = 1; $i <= $cnt; $i++) {
            $this->dataPerfectField[$i] = array("EXAM_SUBCLASS"   => VARS::post("EXAM_SUBCLASS".$i),
                                                "STEPS"           => VARS::post("STEPS".$i),
                                                "LABEL"           => VARS::post("LABEL".$i),
                                                "VALUE"           => VARS::post("VALUE".$i),
                                               );

            //満点
            if ($this->mantendiv == "1") {
                $this->dataPerfectField[$i]["STEPS"] = $this->mantenMark;
                $this->dataPerfectField[$i]["LABEL"] = $this->mantenMark;
            }
        }

        //入力チェック
        if ($this->validateInputPerfect()) {
            knjl002vQuery::getInsertPerfectQuery($this);
            $this->setMessage("MSG201");
        }
        return;
    }

    /**
     * 変数をクリアします
     */
    public function clean()
    {
    }
}
