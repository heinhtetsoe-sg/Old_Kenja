<?php

require_once('for_php7.php');

class knjz419_2Model extends Model {
    var $field      = array();
    var $cmd;
    var $questionnairecd = "";        //primary

    function knjz419_2Model() {}

    function init() {
        $this->field = array("QUESTIONNAIRECD"     =>  VARS::post("QUESTIONNAIRECD"),
                             "QUESTIONNAIRENAME"   =>  VARS::post("QUESTIONNAIRENAME"),
                             "UPDATED"             =>  VARS::post("UPDATED")
                             );

        $this->cmd = VARS::request("cmd");
        if (VARS::get("mode") == "1") {
            unset($this->questionnairecd);
        }
        $temp = VARS::get("QUESTIONNAIRECD");
        if (isset($temp)) {
            $this->questionnairecd = VARS::get("QUESTIONNAIRECD");
        }
    }

    //入力項目チェック
    function validateInput() {
        //キーのチェック
        if (trim($this->field["QUESTIONNAIRECD"]) == "" && $this->cmd == "add") {
            $this->setWarning("MSG301","(進路アンケートコード)");
            return false;
        }
        if ($this->questionnairecd == "" && $this->cmd == "update") {
            $this->setWarning("MSG308","キー値は変更できません");
            return false;
        }
        if ($this->cmd == "update" && $this->questionnairecd != $this->field["QUESTIONNAIRECD"]) {
            $this->setWarning("MSG308","キー値は変更できません");
            return;
        }
        //１レコード取得
        $row = knjz419_2Query::getRow($this->field["QUESTIONNAIRECD"]);
        if ($this->cmd == "update" && !is_array($row)) {
            $this->setWarning("MSG303");
            return false;
        }
        //重複チェック
        if ($this->cmd == "add" && is_array($row)) {
            $this->setWarning("MSG918", "(進路アンケートコード)");
            return false;
        }
        //更新日付チェック
        if ($this->cmd == "update" && $row["UPDATED"] != $this->field["UPDATED"]) {
            $this->setWarning("MSG307");
            return false;
        }
        //文字数チェック
        if (strlen($this->field["QUESTIONNAIRENAME"]) == 0) {
            $this->setWarning("MSG301","(進路アンケート名称)");
            return false;
        } elseif (strlen($this->field["QUESTIONNAIRENAME"]) > 120 ) {
            $this->setWarning("MSG915", "(進路アンケート名称) 120バイトまで");
            return false;
        }
        return true;
    }
    //編集完了用のモデルを返します
    function &getUpdateModel() {
        //入力チェック
        if ($this->validateInput()) {
            knjz419_2Query::getUpdateQuery($this->field);
            $this->setMessage("MSG201");
        }
    }
    function &getInsertModel() {
        //入力チェック
        if ($this->validateInput()) {
            knjz419_2Query::getInsertQuery($this->field);
            $this->questionnairecd = $this->field["QUESTIONNAIRECD"];
            $this->setMessage("MSG201");
        }
        return;
    }

    //DELETE
    function &getDeleteModel() {
        //キーの更新チェック
        if ($this->questionnairecd == "") {
            $this->setWarning("MSG308","キー値は変更できません");
            return false;
        }
        if ($this->questionnairecd != $this->field["QUESTIONNAIRECD"]) {
            $this->setWarning("MSG308","キー値は変更できません");
            return;
        }
        $row = knjz419_2Query::getRow($this->questionnairecd);
        if (!is_array($row)) {
            $this->setWarning("MSG303");
            return;
        }
        if (knjz419_2Query::IsExisting($this->questionnairecd)) {
            $this->setWarning("MSG919", "年度データに登録されています。");
            return;
        }
        knjz419_2Query::getDeleteQuery($this->field);
        $this->questionnairecd = "";
        $this->setMessage("MSG201");
    }

    function clean() {}
}
?>
