<?php

require_once('for_php7.php');

class lzsearchModel extends Model {
    var $cmd;
    var $company_cd;        			//primary
	var $usr_auth;

    function lzsearchModel()
    {
        common::GetControlMaster_Fnc($this->control);
	}

    function init()
    {
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

        //����
        if ($this->cmd == "search") {
            $this->search_fields = array("company_name"		  => VARS::post("COMPANY_NAME"),
                                         "shushoku_add"		  => VARS::post("SHUSHOKU_ADD"),
                                         "year"		      => $this->control["ǯ��"]);
        }
        if ($this->cmd == "search_view" || VARS::get("PROGRAMID")) {
            unset($this->search_fields);
        }
    }
}
?>
