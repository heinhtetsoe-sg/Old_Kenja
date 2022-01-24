<?php
class knjlz11hModel extends Model {
    var $cmd;
    var $examYear;
    var $leftYear = "";
    var $applicantdiv;
    var $qualifiedCd = "";
    var $qualifiedJudgeCd;
    var $field = array();

    function knjlz11hModel() {
    }

    function init() {
        //起動時に初期化
        if (VARS::get("PROGRAMID")) {
            unset($this->leftYear);
            unset($this->applicantdiv);
            unset($this->qualifiedCd);
            unset($this->qualifiedJudgeCd);
        }

        //プロパティファイル
        $this->getPropertiesAll();

        $this->cmd = VARS::request("cmd");

        $this->examYear  = CTRL_YEAR + 1; // 入試年度

        if (VARS::request("ENTEXAMYEAR") != "") {
            $this->leftYear = VARS::request("ENTEXAMYEAR");
        }
        $this->leftYear = ($this->leftYear == "") ? $this->examYear : $this->leftYear;

        if (VARS::request("APPLICANTDIV") != "") {
            $this->applicantdiv = VARS::request("APPLICANTDIV");
        }

        if (VARS::request("QUALIFIED_CD") != "") {
            $this->qualifiedCd = VARS::request("QUALIFIED_CD");
        }

        if (VARS::get("QUALIFIED_JUDGE_CD") != "") {
            $this->qualifiedJudgeCd = VARS::get("QUALIFIED_JUDGE_CD");
        }

        $this->field = array(
                        "QUALIFIED_JUDGE_CD" => VARS::post("QUALIFIED_JUDGE_CD"),
                        "QUALIFIED_NAME"     => VARS::post("QUALIFIED_NAME"),
                        "QUALIFIED_ABBV"     => VARS::post("QUALIFIED_ABBV"),
                        "PLUS_POINT"         => VARS::post("PLUS_POINT"),
                        );
    }

    //コピーのモデルを返します
    function &getCopyModel()
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $nextYear = $this->leftYear + 1;
        $message = '';
        $flg = true;

        //コピー元データの存在確認
        if ($this->checkQualifiedMstYear($db, $this->leftYear) <= 0) {
            $message = '作成するデータが存在しません。';
            $flg = false;
        }
        //コピー先データの存在確認
        if ($this->checkQualifiedMstYear($db, $nextYear) > 0) {
            $message = $message . '既に次年度のデータが作成されています。';
            $flg = false;
        }

        if ($flg) {
            $query = knjlz11hQuery::getCopyQualifiedMstQuery($this);
            $db->query($query);
            $this->setMessage("MSG201");
        } else {
            $this->setMessage("MSG203", "\\n" . $message);
        }

        $db->commit();
        Query::dbCheckIn($db);
    }

    function checkQualifiedMstYear($db, $year)
    {
        $query = knjlz11hQuery::getCheckQualifiedMstSql($year);
        $result = $db->getOne($query);
        return $result;
    }

    //入力項目チェック
    function validateInput() {

        //ゼロ埋め
        if ($this->field["QUALIFIED_JUDGE_CD"] != "") {
            $this->field["QUALIFIED_JUDGE_CD"] = sprintf("%02d", $this->field["QUALIFIED_JUDGE_CD"]);
        }

        //キー変更チェック
        if ($this->cmd == "update" || $this->cmd == "delete") {
            $flg = false;
            if ($this->qualifiedJudgeCd != $this->field["QUALIFIED_JUDGE_CD"]) {
                $this->setWarning("MSG308");
                return false;
            }
        }

        //必須チェック
        if ($this->cmd == "add" || $this->cmd == "update" || $this->cmd == "delete") {
            if ($this->field["QUALIFIED_JUDGE_CD"] == "") {
                $this->setWarning("MSG301", "( 入試資格判定ＣＤ )");
                return false;
            }
        }
        if ($this->cmd == "add" || $this->cmd == "update") {
            if ($this->field["QUALIFIED_NAME"] == "") {
                $this->setWarning("MSG301", "( 入試資格名称 )");
                return false;
            }
        }

        //１レコード取得
        $query = knjlz11hQuery::getRow($this, "chk");
        $db = Query::dbCheckOut(); //DB接続
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);     //DB切断
        if (($this->cmd == "update" || $this->cmd == "delete") && !is_array($row)) {
            $this->setWarning("MSG303");
            return false;
        }
        //重複チェック
        if ($this->cmd == "add" && is_array($row)) {
            $this->setWarning("MSG918", "{$setMsseage}");
            return false;
        }

        //文字数チェック
        $checkStrLen = array();
        $checkStrLen["QUALIFIED_JUDGE_CD"] = array("LEN" => 2,   "MESAGE" => "入試資格判定ＣＤ");
        $checkStrLen["QUALIFIED_NAME"]     = array("LEN" => 300, "MESAGE" => "入試資格名称");
        $checkStrLen["QUALIFIED_ABBV"]     = array("LEN" => 30,  "MESAGE" => "入試資格略称");
        foreach ($checkStrLen as $key => $val) {
            if (strlen($this->field[$key]) > $val["LEN"] ) {
                $this->setWarning("MSG915", $val["MESAGE"]." ".$val["LEN"]."バイトまで");
                return false;
            }
        }

        return true;
    }

    //編集完了用のモデルを返します
    function &getUpdateModel() {
        //入力チェック
        if ($this->validateInput()) {
            $this->update();
            $this->qualifiedJudgeCd = $this->field["QUALIFIED_JUDGE_CD"];
            $this->setMessage("MSG201");
        }
    }
    //アップデート時の内部メソッド
    function &update() {
        $result = knjlz11hQuery::getUpdateQuery($this, $this->field);
        return $result;
    }

    function &getInsertModel() {
        //入力チェック
        if ($this->validateInput()) {
            $this->insert();
            $this->qualifiedJudgeCd = $this->field["QUALIFIED_JUDGE_CD"];
            $this->setMessage("MSG201");
        }
        return;
    }
    //インサート
    function &insert() {
        $result = knjlz11hQuery::getInsertQuery($this, $this->field);
        return $result;
    }

    //DELETE
    function &getDeleteModel() {
        if ($this->validateInput()) {
            knjlz11hQuery::getDeleteQuery($this, $this->field);
            $this->qualifiedJudgeCd = "";
            $this->setMessage("MSG201");        
            $this->field = array();
        }
    }
    /**
     * 変数をクリアします
     */
    function clean() {
    }
}
?>
