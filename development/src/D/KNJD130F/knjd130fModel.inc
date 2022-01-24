<?php

require_once('for_php7.php');
class knjd130fModel extends Model {
    var $cmd;
    var $schregno = "";
    var $name = "";
    var $grade;
    var $hr_class;
    var $semester;
    var $field = array();

    function knjd130fModel() {}

    function init() {
        if (VARS::get("PROGRAMID")!="") {
            unset($this->schregno);
        }
        if (VARS::get("PROGRAMID")!="") {
            unset($this->name);
            $this->semester = CTRL_SEMESTER;
        }
        if (VARS::post("SEMESTER")) {
            $this->semester = VARS::post("SEMESTER");
        }
        $this->field = array("SEMESTER"         =>  VARS::post("SEMESTER"),
                             "COMMUNICATION"    =>  VARS::post("COMMUNICATION")   //出欠備考
                             );

        $this->cmd = VARS::request("cmd");

        if (VARS::get("SCHREGNO")!="") {
            $this->schregno   = VARS::get("SCHREGNO");
        }
        if (VARS::get("NAME")!="") {
            $this->name       = VARS::get("NAME");
        }

        $this->grade = VARS::get("GRADE");
        $this->hr_class = VARS::get("HR_CLASS");
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
        if ($this->schregno=="") {
            $this->setWarning("MSG304");
            return false;
        }
        if (strlen($this->field["COMMUNICATION"]) > 178) {
            $this->setWarning("MSG915","( 出欠備考は178バイトまでです )");
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
        knjd130fQuery::getDeleteQuery($this->schregno, $this->field["SEMESTER"]);
        knjd130fQuery::getInsertQuery($this->field,$this->schregno);
        return $result;
    }
}
?>
