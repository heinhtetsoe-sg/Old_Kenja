<?php

require_once('for_php7.php');

class knjxSearch_FreshmanModel extends Model {
    var $cmd;
    var $schregno;
    var $grade;
    var $hr_class;
    var $name;
    var $attendno;
    var $usr_auth;
    var $exeYear;

    function knjxSearch_FreshmanModel()
    {
        common::GetControlMaster_Fnc($this->control);
    }

    function init()
    {
        $this->exeYear = CTRL_YEAR + 1;

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

        //検索
        if ($this->cmd == "search") {
            $this->search_fields = array("class"          => VARS::post("GRADE_HR_CLASS"),
                                         "kanji"          => VARS::post("KANJI"),
                                         "kana"           => VARS::post("KANA"));
        }
        if ($this->cmd == "search_view" || VARS::get("PROGRAMID")) {
            unset($this->search_fields);
        }
        $this->getPropertiesAll(); //プロパティファイルの読込み
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
        if (VARS::get("SEND_SchoolKind")) {
            $this->setSchoolKind = VARS::get("SEND_SchoolKind");
        }
    }
}
?>
