<?php

require_once('for_php7.php');

class knjxsearch9_job_ssModel extends Model
{
    public $cmd;
    public $company_cd;//primary
    public $usr_auth;

    public function knjxsearch9_job_ssModel()
    {
        common::GetControlMaster_Fnc($this->control);
    }

    public function init()
    {
        if (VARS::get("PATH")) {
            $this->programid = "";
            $this->path      = VARS::get("PATH");
        }
        if (VARS::get("PROGRAMID")) {
            $this->programid = VARS::get("PROGRAMID");
        }
        $this->usr_auth = common::SecurityCheck(STAFFCD, $this->programid);
        $this->cmd      = VARS::request("cmd");

        //検索
        if ($this->cmd == "search") {
            $this->search_fields = array("company_name"  => VARS::post("COMPANY_NAME"),
                                         "shushoku_addr" => VARS::post("SHUSHOKU_ADDR"),
                                         "year"          => $this->control["年度"]);
        }
        if ($this->cmd == "search_view" || VARS::get("PROGRAMID")) {
            unset($this->search_fields);
        }
    }
}
