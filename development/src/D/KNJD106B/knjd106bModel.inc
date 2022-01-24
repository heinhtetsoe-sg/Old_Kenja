<?php

require_once('for_php7.php');
class knjd106bModel extends Model {

    var $cmd;
    var $schregno = "";
    var $name = "";
    var $grade;
    var $hr_class;
    var $field = array();

    function knjd106bModel()
    {
    }

    function init()
    {
        if (VARS::get("PROGRAMID")!="") {
            unset($this->schregno);
            unset($this->name);
        }
        $this->field = array("TESTCD"           =>  VARS::post("TESTCD"),               //テスト種別コンボ
                             "TOTALSTUDYTIME"   =>  VARS::post("TOTALSTUDYTIME")        //通信欄
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
        if ($this->schregno == "") {
            $this->setWarning("MSG304");
            return false;
        }
        if (strlen($this->field["TOTALSTUDYTIME"]) > 409) {
            if(substr($this->field["TESTCD"], 0, 1) == '1'){
                $this->setWarning("MSG915","( 模試評は409バイトまでです )");
            } else {
                $this->setWarning("MSG915","( 実力評は409バイトまでです )");
            }
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
        knjd106bQuery::getInsertQuery($this->field, $this->schregno);
        return $result;
    }

}
?>
