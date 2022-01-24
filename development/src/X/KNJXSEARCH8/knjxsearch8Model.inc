<?php

require_once('for_php7.php');
// kanji=漢字
// $Id: knjxsearch8Model.inc 56591 2017-10-22 13:04:39Z maeshiro $
class knjxsearch8Model extends Model {
    var $cmd;
    var $school_cd;        			//primary
	var $usr_auth;

    function knjxsearch8Model()
    {
        common::GetControlMaster_Fnc($this->control);
	}

    function init()
    {

        if(VARS::get("PROGRAMID")){
            $this->programid = VARS::get("PROGRAMID");
        }
        if(VARS::get("PATH")){
            $this->path = VARS::get("PATH");
        }
        $this->usr_auth = common::SecurityCheck(STAFFCD, $this->programid);
        $this->cmd          = VARS::request("cmd");

        //検索
        if ($this->cmd == "search") {
            $this->search_fields = array("school_name"		  => VARS::post("SCHOOL_NAME"),
                                         "buname"		  => VARS::post("BUNAME"),
                                         "kaname"		  => VARS::post("KANAME"),
                                         "year"		      => $this->control["年度"]);
        }
        if ($this->cmd == "search_view" || VARS::get("PROGRAMID")) {
            unset($this->search_fields);
        }
    }
}
?>
