<?php

require_once('for_php7.php');
class knjd131eModel extends Model {
    var $cmd;
    var $schregno = "";
    var $name = "";
    var $grade;
    var $hr_class;
    var $grade_hrclass;
    var $field = array();

    function knjd131eModel() {}

    function init() {
        if (VARS::get("PROGRAMID")!="") {
            unset($this->schregno);
            unset($this->name);
        }

        $this->field = array("SPECIALACTREMARK"     =>  VARS::post("SPECIALACTREMARK"),     //担任より
                             "SEMESTER"             =>  VARS::post("SEMESTER"),             //学期
                             "PATARN_DIV"           =>  VARS::post("PATARN_DIV")            //帳票パターン
                             );

        $this->cmd = VARS::request("cmd");

        if (VARS::get("SCHREGNO")!="") {
            $this->schregno = VARS::get("SCHREGNO");
        }
        if (VARS::get("NAME")!="") {
            $this->name     = VARS::get("NAME");
        }

        $this->grade    = VARS::get("GRADE");
        $this->hr_class = VARS::get("HR_CLASS");
        if (VARS::get("cmd") == "replace") {
            $this->grade_hrclass = VARS::request("GRADE_HRCLASS");
        }

        //リストの年組コンボが変更されたとき
        if (VARS::get("init")) {
            unset($this->schregno);
            unset($this->name);
        }
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

    }

    //入力項目チェック
    function validateInput() {
        if ($this->schregno == "") {
            $this->setWarning("MSG304");
            return false;
        }
        if (strlen($this->field["SPECIALACTREMARK"]) > 318) {
            $this->setWarning("MSG915","( 特別活動の記録は318バイトまでです )");
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
        if ($this->cmd == 'update') {
            knjd131eQuery::getDeleteQuery($this->schregno, $this);
            knjd131eQuery::getInsertQuery($this->field, $this->schregno);
        }
        return $result;
    }
}
?>
