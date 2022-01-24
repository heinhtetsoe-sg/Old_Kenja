<?php

require_once('for_php7.php');

class knjh439Model extends Model {
    var $field      = array();
    var $cmd;
    var $mockcd = "";        //primary
    var $grade = "";       //primary
    var $mockyear;
    var $copyyear;

    function knjh439Model()
    {
    }

    function init()
    {
        $this->getPropertiesAll(); //プロパティファイル
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        //駿台甲府学園か確認
        //DB接続
        $db = Query::dbCheckOut();
        $this->CHECK_SCHOOL = $db->getOne(knjh439Query::getCheck_School());
        //DB切断
        Query::dbCheckIn($db);

        $this->field = array("MOCKCD"       =>  VARS::post("MOCKCD"),
                             "GRADE"        =>  "0".substr(VARS::post("MOCKCD"),6,1),
                             "K_FIELD_CNT"    =>  VARS::post("K_FIELD_CNT"),
                             "H_FIELD_CNT"    =>  VARS::post("H_FIELD_CNT")
                             );

        $this->cmd          = VARS::request("cmd");
        $this->copyyear = (VARS::request("COPYYEAR")) ? VARS::request("COPYYEAR") : $this->copyyear;
        $this->mockyear = (VARS::request("MOCKYEAR")) ? VARS::request("MOCKYEAR") : $this->mockyear;
        if (VARS::get("PROGRAMID")) {
            unset($this->copyyear);
            unset($this->mockyear);
        }
        if ($this->cmd == "changeMockyear" || VARS::get("PROGRAMID")) {
            unset($this->mockcd);
            unset($this->grade);
        }
        if (VARS::get("MOCKCD")) {
            $this->mockcd = VARS::get("MOCKCD");
        }
        if (VARS::get("GRADE")) {
            $this->grade = VARS::get("GRADE") == "" ? "0".substr($this->mockcd,6,1) : VARS::get("GRADE");
        }
    }

    //入力項目チェック
    function validateInput()
    {
        //キーのチェック
        if (($this->field["GRADE"] == "" || $this->field["MOCKCD"] == "") && $this->cmd == "add") {
            $this->setWarning("MSG301","(模試名称・学年)");
            return false;
        }

        if (($this->grade == "" || $this->mockcd == "") && $this->cmd == "update") {
            $this->setWarning("MSG308","キー値は変更できません");
            return false;
        }
        if ($this->cmd == "update" && ($this->grade != $this->field["GRADE"] || $this->mockcd != $this->field["MOCKCD"])) {
            $this->setWarning("MSG308","キー値は変更できません");
            return;
        }
        //１レコード取得
        $row = knjh439Query::getRow($this->mockyear, $this->field["MOCKCD"], $this->field["GRADE"]);
        if ($this->cmd == "update" && !is_array($row)) {
            $this->setWarning("MSG303");
            return false;
        }
        //重複チェック
        if ($this->cmd == "add" && is_array($row)){
            $this->setWarning("MSG302");
            return false;
        }
        return true;
    }
    //編集完了用のモデルを返します
    function &getUpdateModel()
    {
        //入力チェック
        if ($this->validateInput()){
            $this->update();
            $this->setMessage("MSG201");
        }    
    }
    function &getInsertModel()
    {
        //入力チェック
        if ($this->validateInput()){
            $this->insert();
            $this->mockcd = $this->field["MOCKCD"];
            $this->grade = $this->field["GRADE"];
            $this->setMessage("MSG201");
        }
        return;
    }

    //DELETE
    function &getDeleteModel() {
        //キーの更新チェック
        if ($this->grade == "" || $this->mockcd == "") {
            $this->setWarning("MSG308","キー値は変更できません");
            return false;
        }
        if ($this->grade != $this->field["GRADE"] || $this->mockcd != $this->field["MOCKCD"]) {
            $this->setWarning("MSG308","キー値は変更できません");
            return;
        }
        $row = knjh439Query::getRow($this->mockyear, $this->mockcd, $this->grade);
        if (!is_array($row)){
            $this->setWarning("MSG303");
            return;    
        }
        knjh439Query::getDeleteQuery($this);
        $this->mockcd = "";
        $this->grade = "";
        $this->setMessage("MSG201");
    }

    function &insert() {
        $result = knjh439Query::getInsertQuery($this);
        return $result;
    }
    //アップデート時の内部メソッド
    function &update() {
        $result = knjh439Query::getUpdateQuery($this);
        return $result;
    }

    //データコピー
    function &getCopyModel() {
        if (!$this->copyCheck()) {
            return;
        }
        knjh439Query::getCopyQuery($this);
        $this->setMessage("MSG201");

        return;
    }
    //コピーチェック
    function copyCheck() {
        //DB接続
        $db = Query::dbCheckOut();

        if (0 < $db->getOne(knjh439Query::getCopyCheck($this))) {
            $this->setWarning("対象年度のデータが既に存在しています。");
            return false;
        }

        //DB切断
        Query::dbCheckIn($db);

        return true;
    }

    /**
     * 変数をクリアします
     */
    function clean()
    {
    }
}
?>
