<?php

require_once('for_php7.php');
class knjd105bModel extends Model {

    var $cmd;
    var $schregno = "";
    var $name = "";
    var $grade;
    var $hr_class;
    var $field = array();

    function knjd105bModel()
    {
    }

    function init()
    {
        if (VARS::get("PROGRAMID")!="") {
            unset($this->schregno);
            unset($this->name);
        }
        $this->field = array("SEMESTER"         =>  VARS::post("SEMESTER"),             //学期コンボ
                             "TESTCD"           =>  VARS::post("TESTCD"),               //テスト種別コンボ
                             "TOTALSTUDYTIME"   =>  VARS::post("TOTALSTUDYTIME")        //通信欄
                             );

        $this->cmd = VARS::request("cmd");

        if (VARS::get("SCHREGNO")!="") {
            $this->schregno   = VARS::get("SCHREGNO");
        }
        if (VARS::get("NAME")!="") {
            $this->name       = VARS::get("NAME");
        }
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
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
        if (strlen($this->field["TOTALSTUDYTIME"]) > 409) {
            if(substr($this->field["TESTCD"],0,2) == '01'){
                $this->setWarning("MSG915","( 個人評は409バイトまでです )");
            } else {
                $this->setWarning("MSG915","( 注意事項は409バイトまでです )");
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
        knjd105bQuery::getInsertQuery($this->field, $this->schregno);
        return $result;
    }

}
?>
