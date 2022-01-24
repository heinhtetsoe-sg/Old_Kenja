<?php

require_once('for_php7.php');

class knjxsearch_bankModel extends Model {
    var $cmd;
    var $schregno;                  //primary
    var $grade;
    var $hr_class;
    var $name;
    var $attendno;
    var $usr_auth;

    function knjxsearch_bankModel() {
        common::GetControlMaster_Fnc($this->control);
        $this->control["学期"] = $this->control["学期数"]; //最終学期にする
    }

    function init() {
        if(VARS::get("PROGRAMID")){
            $this->programid = VARS::get("PROGRAMID");
        }
        if ($this->cmd == "chg_grade" || VARS::get("PROGRAMID")) {
            unset($this->search_fields);
            unset($this->grade);
        }
        if(VARS::get("TARGET")){
            $this->target = VARS::get("TARGET");
        }
        if(VARS::get("PATH")){
            $this->path = VARS::get("PATH");
        }
        $this->usr_auth = common::SecurityCheck(STAFFCD, $this->programid);
        $this->cmd          = VARS::request("cmd");
        
        if (VARS::post("GRADE")) {
            $this->grade = VARS::post("GRADE");
        }
        //検索
        if ($this->cmd == "search" || $this->cmd == "search_view2") {
            list($bankCd, $branchCd) = explode("-", VARS::post("BRANCHCD"));
            $this->search_fields = array("HR_CLASS"       => VARS::post("HR_CLASS"),
                                         "SRCH_SCHREGNO"  => VARS::post("SRCH_SCHREGNO"),
                                         "NAME"           => VARS::post("NAME"),
                                         "NAME_SHOW"      => VARS::post("NAME_SHOW"),
                                         "NAME_KANA"      => VARS::post("NAME_KANA"),
                                         "NAME_ENG"       => VARS::post("NAME_ENG"),
                                         "BANKCD"         => VARS::post("BANKCD"),
                                         "BRANCHCD"       => $branchCd,
                                         "DEPOSIT_ITEM"   => VARS::post("DEPOSIT_ITEM"),
                                         "ACCOUNTNO"      => VARS::post("ACCOUNTNO")
                                         );
        }
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }
}
?>
