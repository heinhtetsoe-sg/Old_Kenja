<?php

require_once('for_php7.php');
class knjd131kModel extends Model {
    var $cmd;
    var $schregno = "";
    var $name = "";
    var $grade;
    var $hr_class;
    var $grade_hrclass;
    var $field = array();
    var $selectdata;

    function knjd131kModel() {}

    function init() {
        if (VARS::get("PROGRAMID")!="") {
            unset($this->schregno);
            unset($this->name);
        }

        $this->field = array("DIV4_REMARK"     =>  VARS::post("DIV4_REMARK"),     //ＣＡＳの記録
                             "GRADE"           =>  VARS::post("GRADE")            //学年
                             );

        $this->cmd = VARS::request("cmd");
        if (!$this->field["SEMESTER"]) {
            $this->field["SEMESTER"] = CTRL_SEMESTER;
        }
        if (VARS::get("SEMESTER")!="") {
            $this->field["SEMESTER"] = VARS::get("SEMESTER");
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

        $this->grade    = VARS::request("GRADE");
        $this->hr_class = VARS::get("HR_CLASS");
        if (VARS::get("cmd") == "replace") {
            $this->grade_hrclass = VARS::request("GRADE_HRCLASS");
        }

        //リストの年組コンボが変更されたとき
        if (VARS::get("init")) {
            unset($this->schregno);
            unset($this->name);
        }

        //プロパティファイル
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
        if ($this->validate_row_cnt($this->field["DIV4_REMARK"], (30 * 2)) > 20) {
            $this->setWarning("MSG915","( ＣＡＳの記録は20行までです )");
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
        knjd131kQuery::getInsertQuery($this->field, $this->schregno);
        
        return $result;
    }
}
?>
