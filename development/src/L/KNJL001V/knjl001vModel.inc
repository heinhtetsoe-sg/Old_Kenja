<?php
class knjl001vModel extends Model
{
    public $field = array();
    public $cmd;
    public $examyear;
    public $leftYear = "";
    public $schoolKind = "";
    public $applicantDiv = "";

    public function knjl001vModel()
    {
    }

    public function init()
    {
        //起動時に初期化
        if (VARS::get("PROGRAMID")) {
            unset($this->leftYear);
            unset($this->schoolKind);
            unset($this->applicantDiv);
        }

        //プロパティファイル
        $this->getPropertiesAll();

        $this->cmd = VARS::request("cmd");

        $this->examyear  = CTRL_YEAR + 1;

        $this->field = array("APPLICANT_DIV"         => VARS::post("APPLICANT_DIV"),
                             "APPLICANT_NAME"        => VARS::post("APPLICANT_NAME"),
                            );

        //左画面 年度コンボ
        if (VARS::request("YEAR") != "") {
            $this->leftYear = VARS::request("YEAR");
        }
        $this->leftYear = ($this->leftYear == "") ? $this->examyear : $this->leftYear;

        if (VARS::request("EXAM_SCHOOL_KIND") != "") {
            $this->schoolKind = VARS::request("EXAM_SCHOOL_KIND");
        }

        if (VARS::get("APPLICANT_DIV") != "") {
            $this->applicantDiv = VARS::get("APPLICANT_DIV");
        }
    }

    //コピーのモデルを返します
    public function &getCopyModel()
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $message = '';
        $flg = true;

        $query = knjl001vQuery::getCheckApplicantDivMstSql($this->leftYear);
        $result = $db->getOne($query);
        if ($result > 0) {
            $message = '今年度のデータが存在します。';
            $flg = false;
        }

        $prevYear = $this->leftYear - 1;
        $query = knjl001vQuery::getCheckApplicantDivMstSql($prevYear);
        $result = $db->getOne($query);
        if ($result <= 0) {
            $message = '前年データが存在しません。';
            $flg = false;
        }

        if ($flg) {
            $query = knjl001vQuery::getCopyQuery($db, $this, $prevYear);
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
        //空文字チェック
        if (strlen($this->field["APPLICANT_DIV"]) == 0) {
            $this->setWarning("MSG301", "( 入試コード )");
            return false;
        }
        if (strlen($this->field["APPLICANT_NAME"]) == 0) {
            $this->setWarning("MSG301", "( 入試名称 )");
            return false;
        }

        //キー変更チェック
        if ($this->cmd == "update" || $this->cmd == "delete") {
            if ($this->applicantDiv != $this->field["APPLICANT_DIV"]) {
                $this->setWarning("MSG308");
                return false;
            }
        }

        //必須チェック
        if ($this->cmd == "add" || $this->cmd == "update" || $this->cmd == "delete") {
            if (strlen($this->schoolKind) == 0) {
                $this->setWarning("MSG301", "( 校種 )");
                return false;
            }
            if (strlen($this->field["APPLICANT_DIV"]) == 0) {
                $this->setWarning("MSG301", "( 入試コード )");
                return false;
            }
        }
        if ($this->cmd == "add" || $this->cmd == "update") {
            if (strlen($this->field["APPLICANT_NAME"]) == 0) {
                $this->setWarning("MSG301", "( 入試名称 )");
                return false;
            }
        }

        //コードのゼロ埋め
        $this->field["APPLICANT_DIV"] = sprintf("%02d", $this->field["APPLICANT_DIV"]);

        //１レコード取得
        $query = knjl001vQuery::getRow($this, "chk");
        $db = Query::dbCheckOut(); //DB接続
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);     //DB切断

        //存在チェック
        if (($this->cmd == "update" || $this->cmd == "delete") && !is_array($row)) {
            $this->setWarning("MSG303");
            return false;
        }

        //重複チェック
        if ($this->cmd == "add" && is_array($row)) {
            $this->setWarning("MSG918", "( 入試コード )");
            return false;
        }

        //文字数チェック
        $checkStrLen["APPLICANT_DIV"]   = array("LEN" => 2,  "MESAGE" => "入試コード");
        $checkStrLen["APPLICANT_NAME"]  = array("LEN" => 30, "MESAGE" => "入試名称");
        foreach ($checkStrLen as $key => $val) {
            if (strlen($this->field[$key]) > $val["LEN"]) {
                $this->setWarning("MSG915", $val["MESAGE"]." ".$val["LEN"]."バイトまで");
                return false;
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
            $this->applicantDiv = $this->field["APPLICANT_DIV"];
            $this->setMessage("MSG201");
        }
    }
    //アップデート時の内部メソッド
    public function &update()
    {
        $result = knjl001vQuery::getUpdateQuery($this, $this->field);
        return $result;
    }

    public function &getInsertModel()
    {
        //入力チェック
        if ($this->validateInput()) {
            $this->insert();
            $this->applicantDiv = $this->field["APPLICANT_DIV"];
            $this->setMessage("MSG201");
        }
        return;
    }
    //インサート
    public function &insert()
    {
        $result = knjl001vQuery::getInsertQuery($this, $this->field);
        return $result;
    }

    //DELETE
    public function &getDeleteModel()
    {
        if ($this->validateInput()) {
            knjl001vQuery::getDeleteQuery($this, $this->field);
            $this->applicantDiv = "";
            $this->setMessage("MSG201");
            $this->field = array();
        }
    }
    /**
     * 変数をクリアします
     */
    public function clean()
    {
    }
}
