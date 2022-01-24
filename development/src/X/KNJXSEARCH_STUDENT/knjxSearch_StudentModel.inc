<?php

require_once('for_php7.php');

class knjxSearch_StudentModel extends Model {
    var $cmd;
    var $schregno;
    var $grade;
    var $hr_class;
    var $name;
    var $attendno;
    var $usr_auth;

    function knjxSearch_StudentModel()
    {
        common::GetControlMaster_Fnc($this->control);
    }

    function init()
    {
        $this->usr_auth = common::SecurityCheck(STAFFCD, $this->programid);
        $this->cmd = VARS::request("cmd");

        if(VARS::get("PROGRAMID")){
            $this->programid = VARS::get("PROGRAMID");
        }

        if(VARS::get("TARGET")){
            $this->target = VARS::get("TARGET");
        }

        if(VARS::get("PATH")){
            $this->path = VARS::get("PATH");
            $this->searchItem["SCHREGNO"] = VARS::request("S_SCH");
            $this->searchItem["GRADE_HR_CLASS"] = VARS::request("S_HR");
            $this->searchItem["ATTENDNO"] = VARS::request("S_ATTEND");
            $this->searchItem["COURSEMAJOR"] = VARS::request("S_COURSEMAJOR");
            $this->searchItem["COURSECODE"] = VARS::request("S_COURSE");
            $this->searchItem["NAME"] = VARS::request("S_NAME");
            $this->searchItem["NAMESHOW"] = VARS::request("S_NAMESHOW");
            $this->searchItem["KANA"] = VARS::request("S_KANA");
        }
        //����
        if ($this->cmd == "search") {
            $this->search_fields = array("SCHREGNO"     => VARS::post("SCHREGNO"),
                                         "GRADE"        => substr(VARS::post("GRADE_HR_CLASS"), 0, 2),
                                         "HRCLASS"      => substr(VARS::post("GRADE_HR_CLASS"), 2, 3),
                                         "ATTENDNO"     => VARS::post("ATTENDNO"),
                                         "COURSEMAJOR"  => VARS::post("COURSEMAJOR"),
                                         "COURSECODE"   => VARS::post("COURSECODE"),
                                         "NAME"         => VARS::post("NAME"),
                                         "NAMESHOW"     => VARS::post("NAMESHOW"),
                                         "KANA"         => VARS::post("KANA"));
        }

        if ($this->cmd == "search_view" || VARS::get("PROGRAMID")) {
            unset($this->search_fields);
        }
    }
}
?>
