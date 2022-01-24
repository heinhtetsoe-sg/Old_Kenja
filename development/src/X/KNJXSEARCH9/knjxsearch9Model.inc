<?php

require_once('for_php7.php');
// kanji=����
// $Id: knjxsearch9Model.inc 56591 2017-10-22 13:04:39Z maeshiro $
class knjxsearch9Model extends Model {
    var $cmd;
    var $company_cd;        			//primary
	var $usr_auth;

    function knjxsearch9Model()
    {
        common::GetControlMaster_Fnc($this->control);
	}

    function init()
    {
        if(VARS::get("PATH")){
            $this->programid = "";
            $this->path = VARS::get("PATH");
        }
        if(VARS::get("PROGRAMID")){
            $this->programid = VARS::get("PROGRAMID");
        }
        $this->usr_auth = common::SecurityCheck(STAFFCD, $this->programid);
        $this->cmd          = VARS::request("cmd");

        //検索
        if ($this->cmd == "search") {
            $this->search_fields = array("company_name"		  => VARS::post("COMPANY_NAME"),
                                         "shushoku_addr"	  => VARS::post("SHUSHOKU_ADDR"),
                                         "year"               => $this->control["年度"]);
        }
        if ($this->cmd == "search_view" || VARS::get("PROGRAMID")) {
            unset($this->search_fields);
        }
    }
}
?>
