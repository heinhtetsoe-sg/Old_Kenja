<?php

require_once('for_php7.php');
class knjd130Model extends Model {

    var $cmd;
    var $schregno = "";
    var $name = "";
    var $grade;
    var $hr_class;
    var $field = array();

    function knjd130Model()
    {
    }

    function init()
    {
       if (VARS::get("PROGRAMID")!="") {
           unset($this->schregno);
       }
       if (VARS::get("PROGRAMID")!="") {
           unset($this->name);
       }
        $this->field = array("TOTALSTUDYTIME"   =>  VARS::post("TOTALSTUDYTIME")        //総合的な学習の時間
                            ,"COMMUNICATION"    =>  VARS::post("COMMUNICATION")         //通信欄
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
    function validateInput()
    {
        if ($this->schregno=="") {
            $this->setWarning("MSG304");
            return false;
        }
        if (strlen($this->field["TOTALSTUDYTIME"]) > 258) {
            $this->setWarning("MSG915","( 総合的な学習の時間は258バイトまでです )");
            return false;
        }
        if (strlen($this->field["COMMUNICATION"]) > 258) {
            $this->setWarning("MSG915","( 通信欄は258バイトまでです )");
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
        knjd130Query::getDeleteQuery($this->schregno);
        knjd130Query::getInsertQuery($this->field,$this->schregno);
        return $result;
    }
}
?>
