<?php

require_once('for_php7.php');

class knjd135cModel extends Model {
    var $cmd;
    var $schregno = "";
    var $name = "";
    var $grade;
    var $hr_class;
    var $field = array();

    function knjd135cModel() {}

    function init() {
        if (VARS::get("PROGRAMID")!="") {
            unset($this->schregno);
        }
        if (VARS::get("PROGRAMID")!="") {
            unset($this->name);
        }
        $this->field = array("COMMUNICATION" =>  VARS::post("COMMUNICATION"), //担任所見
                             "REMARK3"       =>  VARS::post("REMARK3")        //説明文
                             );

        $this->cmd = VARS::request("cmd");

        if (VARS::get("SCHREGNO")!="") {
            $this->schregno = VARS::get("SCHREGNO");
        }
        if (VARS::get("NAME")!="") {
            $this->name = VARS::get("NAME");
        }

        $this->grade    = VARS::get("GRADE");
        $this->hr_class = VARS::get("HR_CLASS");
        //リストの年組コンボが変更されたとき
        if (VARS::get("init")) {
            unset($this->schregno);
            unset($this->name);
        }
    }

    //入力項目チェック
    function validateInput() {
        if ($this->schregno=="") {
            $this->setWarning("MSG304");
            return false;
        }

        if ($this->validate_row_cnt($this->field["COMMUNICATION"], (30 * 2)) > 5) {
            $this->setWarning("MSG915","(担任所見は5行までです)");
            return false;
        }
        if ($this->validate_row_cnt($this->field["REMARK3"], (50 * 2)) > 3) {
            $this->setWarning("MSG915","(説明文は3行までです)");
            return false;
        }

        return true;
    }

    function &getUpdateModel() {
        if (!$this->validateInput()) {
            return;
        }
        $this->update();
        $this->setMessage("MSG201");
    }

    function &update() {
        knjd135cQuery::getInsertQuery($this->field,$this->schregno);
        return $result;
    }
}
?>
