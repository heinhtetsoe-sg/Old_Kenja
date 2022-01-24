<?php

require_once('for_php7.php');
class knjd236bModel extends Model {

    var $cmd;
    var $schregno = "";
    var $name = "";
    var $field = array();

    function knjd236bModel()
    {
    }

    function init()
    {
        if (VARS::get("PROGRAMID")!="") {
            unset($this->schregno);
            unset($this->name);
        }
        $this->field = array("SEMESTER"     =>  VARS::post("SEMESTER"),     //学期
                             "REMARK1"      =>  VARS::post("REMARK1")       //個人評
                             );

        $this->cmd = VARS::request("cmd");

        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        if (VARS::get("SCHREGNO")!="") {
            $this->schregno = VARS::get("SCHREGNO");
        }
        if (VARS::get("NAME") != "") {
            $this->name = VARS::get("NAME");
        }

        //リストの年組コンボが変更されたとき
        if (VARS::get("init")) {
            unset($this->schregno);
            unset($this->name);
        }
    }

    //入力項目チェック
    function validateInput()
    {
        if ($this->schregno == "") {
            $this->setWarning("MSG304");
            return false;
        }
        if (strlen($this->field["REMARK1"]) > 90) {
            $this->setWarning("MSG915","( 個人評は90バイトまでです )");
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
        knjd236bQuery::getInsertQuery($this->field, $this->schregno);
        return $result;
    }

}
?>
