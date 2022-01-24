<?php

require_once('for_php7.php');

class knjc035bModel extends Model {
    var $cmd;
    var $field = array();
    var $checked;

    function knjc035bModel() {}

    function init() {

        $this->field["GRADE"]       = VARS::post("GRADE");
        $this->field["SEM_MONTH"]   = VARS::post("SEM_MONTH");

        if ($this->field["SEM_MONTH"]) {
            list ($this->field["MONTH"], $this->field["SEMESTER"]) = preg_split("/-/", $this->field["SEM_MONTH"]);
        }

        $this->checked  = VARS::request("CHECKED");
        $this->cmd      = VARS::request("cmd");

        $this->getPropertiesAll(); //プロパティファイルの読込み
    }

    //入力項目チェック
    function validateInput() {
        //必須チェック
        if ($this->field["GRADE"] == "") {
            $this->setWarning("MSG310", '（学年）');
            return false;
        }
        if ($this->field["SEM_MONTH"] == "") {
            $this->setWarning("MSG310", '（削除月）');
            return false;
        }

        //存在チェック
        $db = Query::dbCheckOut();
        $data_cnt = $db->getOne(knjc035bQuery::getTableData($this, "ATTEND_SEMES_DAT", "cnt"));
        Query::dbCheckIn($db);
        if (!$data_cnt) {
            $this->setWarning("MSG303");
            return false;
        }

        return true;
    }

    //出欠データ削除
    function &getDeleteModel() {
        if ($this->validateInput()) {
            knjc035bQuery::getDeleteQuery($this);
            $this->setMessage("MSG201");
        }

        return;
    }

    //履歴データ削除
    function &getHistDeleteModel() {
        //削除対象件数チェック
        if (get_count($this->checked) < 1) {
            $this->setWarning("MSG304");
            return false;
        }

        knjc035bQuery::getHistDeleteQuery($this);
        $this->setMessage("MSG201");

        return;
    }
}
?>
