<?php

require_once('for_php7.php');

class knjz453Model extends Model {
    var $field      = array();
    var $cmd;
    var $leftYear = "";        //primary
    var $sikakuCd = "";        //primary
    var $resultCd = "";        //primary

    function knjz453Model() {
    }

    function init() {
        //起動時に初期化
        if (VARS::get("PROGRAMID")) {
            unset($this->leftYear);
            unset($this->sikakuCd);
            unset($this->resultCd);
            unset($this->grade);
            unset($this->subclassCd);
        }

        //プロパティファイル
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        $this->cmd = VARS::request("cmd");

        if (VARS::request("YEAR") != "") {
            $this->leftYear = VARS::request("YEAR");
        }
        $this->leftYear = ($this->leftYear == "") ? CTRL_YEAR: $this->leftYear;

        if (VARS::request("SUBCLASS") != "") {
            $this->subclassCd = VARS::request("SUBCLASS");
        }
        if (VARS::request("GRADE") != "") {
            $this->grade = VARS::request("GRADE");
        }
        if (VARS::request("SIKAKUCD") != "") {
            $this->sikakuCd = VARS::request("SIKAKUCD");
        }
        $db = Query::dbCheckOut(); //DB接続
        $setSubclassCd = $db->getOne(knjz453Query::getSubclassCd($this));
        $setGrade      = $db->getOne(knjz453Query::getGrade($this));
        $setSikakuCd   = $db->getOne(knjz453Query::getQualifiedMst());
        Query::dbCheckIn($db);     //DB切断
        $this->subclassCd = ($this->subclassCd == "") ? $setSubclassCd: $this->subclassCd;
        $this->grade      = ($this->grade == "")      ? $setGrade     : $this->grade;
        $this->sikakuCd   = ($this->sikakuCd == "")   ? $setSikakuCd  : $this->sikakuCd;

        $this->field = array("RESULT_CD"            =>  VARS::post("RESULT_CD"),        //結果CD
                             "SCORE"                =>  VARS::post("SCORE"),            //評定
                             "CREDIT"               =>  VARS::post("CREDIT")            //増加単位数
                             );

        $temp = VARS::get("RESULT_CD");
        if (isset($temp)) {
            $this->resultCd = VARS::get("RESULT_CD");
        }
    }

    //コピーのモデルを返します
    function &getCopyModel() {
        knjz453Query::getCopyDelRelationMstQuery($this);
        knjz453Query::getCopyRelationMstQuery($this);
        $this->setMessage('MSG201');
    }

    //入力項目チェック
    function validateInput() {
        //キーのチェック
        if (strlen($this->field["RESULT_CD"]) == 0 && $this->cmd == "add") {
            $this->setWarning("MSG301","(結果CD)");
            return false;
        }

        //評定、増加単位、両方nullの時
        if (strlen($this->field["SCORE"]) == 0 && strlen($this->field["CREDIT"]) == 0 && ($this->cmd == "add" || $this->cmd == "update")) {
            $this->setWarning("評定、増加単位\\nどちらかは入力して下さい");
            return false;
        }

        if ($this->cmd == "update" && ($this->resultCd != $this->field["RESULT_CD"] || $this->resultCd == "")) {
            $this->setWarning("MSG308","キー値は変更できません");
            return;
        }

        //１レコード取得
        $query = knjz453Query::getRow($this, $this->field["RESULT_CD"]);
        $db = Query::dbCheckOut(); //DB接続
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);     //DB切断
        if ($this->cmd == "update" && !is_array($row)) {
            $this->setWarning("MSG303");
            return false;
        }
        //重複チェック
        if ($this->cmd == "add" && is_array($row)) {
            $this->setWarning("MSG918", "結果CD");
            return false;
        }
        return true;
    }

    //編集完了用のモデルを返します
    function &getUpdateModel() {
        //入力チェック
        if ($this->validateInput()) {
            $this->update();
            $this->setMessage("MSG201");
        }
    }
    //アップデート時の内部メソッド
    function &update() {
        $result = knjz453Query::getUpdateQuery($this, $this->field);
        return $result;
    }

    function &getInsertModel() {
        //入力チェック
        if ($this->validateInput()) {
            $this->insert();
            $this->resultCd = $this->field["RESULT_CD"];
            $this->setMessage("MSG201");
        }
        return;
    }
    //インサート
    function &insert() {
        $result = knjz453Query::getInsertQuery($this, $this->field);
        return $result;
    }

    //DELETE
    function &getDeleteModel() {

        //キーの更新チェック
        if ($this->resultCd == "") {
            $this->setWarning("MSG308","キー値は変更できません");
            return false;
        }
        if ($this->resultCd != $this->field["RESULT_CD"]) {
            $this->setWarning("MSG308","キー値は変更できません");
            return;
        }

        $query = knjz453Query::getRow($this, $this->field["RESULT_CD"]);
        $db = Query::dbCheckOut(); //DB接続
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);     //DB切断
        if (!is_array($row)) {
            $this->setWarning("MSG303");
            return;
        }
        knjz453Query::getDeleteQuery($this, $this->field);
        $this->resultCd = "";
        $this->testCd   = "";
        $this->setMessage("MSG201");
        
        $this->field = array();
    }
    /**
     * 変数をクリアします
     */
    function clean() {
    }
}
?>
