<?php

require_once('for_php7.php');

class knjxsearch7Model extends Model {
    var $cmd;
    var $schregno;                  //primary
    var $grade;
    var $hr_class;
    var $name;
    var $attendno;
    var $usr_auth;

    function knjxsearch7Model()
    {
        common::GetControlMaster_Fnc($this->control);
    }

    function init()
    {
        if (VARS::get("PROGRAMID")) {
            $this->programid = VARS::get("PROGRAMID");
        }
        if (VARS::get("TARGET")) {
            $this->target = VARS::get("TARGET");
        }
        if (VARS::get("PATH")) {
            $this->path = VARS::get("PATH");
        }
        $this->usr_auth = common::SecurityCheck(STAFFCD, $this->programid);
        $this->cmd = VARS::request("cmd");

        //検索
        if ($this->cmd == "search" || $this->cmd == "search_view2") {
            $this->search_fields = array("graduate_year"  => VARS::post("GRADUATE_YEAR"),
                                         "graduate_class" => VARS::post("GRADUATE_CLASS"),
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
    }
}
?>
