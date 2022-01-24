<?php

require_once('for_php7.php');

class knjxsearch4Model extends Model {
    var $cmd;
    var $schregno;                  //primary
    var $grade;
    var $hr_class;
    var $name;
    var $attendno;
    var $usr_auth;

    function knjxsearch4Model() {
        common::GetControlMaster_Fnc($this->control);
        $this->control["学期"] = $this->control["学期数"]; //最終学期にする
    }

    function init() {
        if(VARS::get("PROGRAMID")){
            $this->programid = VARS::get("PROGRAMID");
        }
        if(VARS::get("TARGET")){
            $this->target = VARS::get("TARGET");
        }
        if(VARS::get("PATH")){
            $this->path = VARS::get("PATH");
        }
        $this->usr_auth = common::SecurityCheck(STAFFCD, $this->programid);
        $this->cmd          = VARS::request("cmd");

        //検索
        if ($this->cmd == "search" || $this->cmd == "search_view2") {
            $this->search_fields = array("graduate_year"  => VARS::post("GRADUATE_YEAR"),
                                         "graduate_class" => VARS::post("GRADUATE_CLASS"),
                                         "lkanji"         => VARS::post("LKANJI"),
                                         "lkana"          => VARS::post("LKANA"));
        }
        if ($this->cmd == "search_view" || VARS::get("PROGRAMID")) {
            unset($this->search_fields);
        }
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }
}
?>
