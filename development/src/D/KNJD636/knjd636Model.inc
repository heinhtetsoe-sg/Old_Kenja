<?php

require_once('for_php7.php');
class knjd636Model extends Model {

    var $cmd;
    var $schregno = "";
    var $name = "";
    var $grade;
    var $hr_class;
    var $field = array();

    function knjd636Model(){}

    function init()
    {
       if (VARS::get("PROGRAMID")!="") {
           unset($this->schregno);
           unset($this->name);
       }
        $this->field = array("TOTALSTUDYTIME" => VARS::post("TOTALSTUDYTIME"), //通信欄
                             "SEMESTER"       => VARS::post("SEMESTER")        //学期
                             );

        $this->cmd = VARS::request("cmd");
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

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
    }

    //入力項目チェック
    function validateInput()
    {
        if ($this->schregno=="") {
            $this->setWarning("MSG304");
            return false;
        }
        if (strlen($this->field["TOTALSTUDYTIME"]) > 374) {
            $this->setWarning("MSG915","( 通信欄は374バイトまでです )");
            return false;
        }
        return true;
    }

    function &getUpdateModel()
    {
        if (!$this->validateInput()) {
            return;
        }
        $this->update();
        $this->setMessage("MSG201");
    }

    function &update()
    {
        knjd636Query::getDeleteQuery($this->field["SEMESTER"], $this->schregno);
        knjd636Query::getInsertQuery($this->field, $this->schregno);
        return $result;
    }
}
?>
