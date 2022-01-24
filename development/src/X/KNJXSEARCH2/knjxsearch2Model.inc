<?php

require_once('for_php7.php');

class knjxsearch2Model extends Model {
    var $cmd;
    var $schregno;
    var $grade;
    var $hr_class;
    var $name;
    var $attendno;
    var $usr_auth;

    function knjxsearch2Model()
    {
        common::GetControlMaster_Fnc($this->control);
    }

    function init()
    {
        if (VARS::get("PROGRAMID")) {
            $this->programid = VARS::get("PROGRAMID");
        }
        if (VARS::get("TARGET") || VARS::get("target")) {
            $this->target = VARS::get("TARGET");
            unset($this->schkind);
            unset($this->setSchoolKind);
        }
        if (VARS::get("PATH")) {
            $this->path = VARS::get("PATH");
        }
        $this->usr_auth = common::SecurityCheck(STAFFCD, $this->programid);
        $this->cmd          = VARS::request("cmd");

        if(VARS::request("SCHKIND") != ""){
            $this->schkind = VARS::request("SCHKIND");
        }

        //����
        if ($this->cmd == "search") {
            $this->search_fields = array("grade"          => substr(VARS::post("GRADE_HR_CLASS"),0,2),
                                         "class"          => substr(VARS::post("GRADE_HR_CLASS"),2,3),
                                         "kanji"          => VARS::post("KANJI"),
                                         "kana"           => VARS::post("KANA"),
                                         "year"           => VARS::post("YEAR"),
                                         "semester"       => VARS::post("SEMESTER"));
        }
        if ($this->cmd == "search_view" || VARS::get("PROGRAMID")) {
            unset($this->search_fields);
        }
        $this->getPropertiesAll(); //�ץ��ѥƥ��ե�������ɹ���
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
        if (VARS::get("SEND_SchoolKind")) {
            $this->setSchoolKind = VARS::get("SEND_SchoolKind");
        }
    }
}
?>