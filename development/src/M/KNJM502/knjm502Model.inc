<?php

require_once('for_php7.php');
class knjm502Model extends Model {
    var $cmd;
    var $schregno = "";
    var $name = "";
    var $grade;
    var $hr_class;
    var $grade_hrclass;
    var $field = array();
    var $selectdata;

    function knjm502Model() {}

    function init() {
        if (VARS::get("PROGRAMID")!="") {
            unset($this->schregno);
            unset($this->name);
        }

        $this->field = array("SEMESTER"         =>  VARS::post("SEMESTER"),         //学期
                             "COMMUNICATION"    =>  VARS::post("COMMUNICATION"),    //通信欄
                             );

        $this->cmd = VARS::request("cmd");
        if (!$this->field["SEMESTER"]) {
            $this->field["SEMESTER"] = CTRL_SEMESTER;
        }
        if (VARS::get("SEMESTER")!="") {
            $this->field["SEMESTER"] = VARS::get("SEMESTER");
        }
        
        if ($this->cmd == 'replace_update') {
            $this->field["RCHECK1"] = VARS::post("RCHECK1");
        }
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        if (VARS::get("SCHREGNO")!="") {
            $this->schregno = VARS::get("SCHREGNO");
        }
        if (VARS::get("NAME")!="") {
            $this->name     = VARS::get("NAME");
        }
        if (VARS::post("selectdata") != "") {
            $this->selectdata = VARS::post("selectdata");
        } elseif (VARS::post("cmd") != "replace_update") {
            unset($this->selectdata);
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
    }

    //入力項目チェック
    function validateInput() {
        if ($this->schregno == "") {
            $this->setWarning("MSG304");
            return false;
        }
        if ($this->validate_row_cnt($this->field["COMMUNICATION"], (10 * 2)) > 10) {
            $this->setWarning("MSG915","( 通信欄は10行までです )");
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
            knjm502Query::getDeleteQuery($this->field, $this->schregno);
            knjm502Query::getInsertQuery($this->field, $this->schregno);
        } elseif ($this->cmd == 'replace_update') {
            knjm502Query::getInsertQuery2($this->field, $this->selectdata);
        }
        return $result;
    }
}
?>
