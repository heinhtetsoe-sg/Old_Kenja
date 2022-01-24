<?php

require_once('for_php7.php');
class knjd635Model extends Model {

    var $cmd;
    var $schregno = "";
    var $name = "";
    var $grade;
    var $hr_class;
    var $field = array();
    var $record = array();
    var $gakki;

    function knjd635Model()
    {
    }

    function init()
    {
       if (VARS::get("PROGRAMID")!="") {
           unset($this->schregno);
       }
       if (VARS::get("PROGRAMID")!="") {
           unset($this->name);
           unset($this->gakki);
           $this->record = array();
       }
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        //評価
        for($i=1; $i<11; $i++)
        {
            $this->record["RECORD"][$i] = VARS::post("RECORD".$i);
        }

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
            unset($this->gakki);
        }
        if (VARS::post("GAKKI")!="") {
            $this->gakki = VARS::post("GAKKI");
        }
    }

    //入力項目チェック
    function validateInput()
    {
        if ($this->schregno=="") {
            $this->setWarning("MSG304");
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
        knjd635Query::getDeleteQuery($this->schregno, $this->gakki);
        knjd635Query::getInsertQuery($this, $this->schregno, $this->gakki);
        return $result;
    }
}
?>
