<?php

require_once('for_php7.php');


class knja090_2Model extends Model {
 
    var $cmd;
    var $schregno;
    var $year;
    var $semester;
    var $semester_name;
    var $grade;
    var $hr_class;
    var $selectdata;
    var $course = array();
    
    function knja090_2Model()
    {
    }

    function init()
    {              
        $this->cmd                  = VARS::request("cmd");
        $this->schregno             = VARS::get("SCHREGNO");
        
        if (VARS::get("year") != ""){
            $this->year             = VARS::get("year");
            $this->semester         = VARS::get("semester");
            $this->semester_name    = VARS::get("sn"); 
            $this->grade            = substr(VARS::get("gc"),0,2);
            $this->hr_class         = substr(VARS::get("gc"),3,2);
        }

        
        $this->selectdata    = VARS::post("selectdata");
        if ($this->selectdata == ""){
            $this->selectdata = $this->schregno;
        }
        $this->course         = array("course1"    => VARS::post("course1"),
                                     "course2"    => VARS::post("course2"),
                                     "course3"    => VARS::post("course3"));
    }
    
    function &getUpdateModel()
    {
        $ret = knja090_2Query::GetUpdateQuery($this);
        if ($ret == 1)
            $this->setMessage("MZ0046");
    }
}
?>
