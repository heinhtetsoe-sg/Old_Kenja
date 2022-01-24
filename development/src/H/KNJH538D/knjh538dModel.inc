<?php

require_once('for_php7.php');

class knjh538dModel extends Model {

    var $cmd;
    var $field = array();
    var $reset = array();
    var $unSetSchreg = array();

    function knjh538dModel()
    {
        $this->field = array();
    }

    function init()
    {
        if (VARS::get("PROGRAMID")) {
            unset($this->getPrgId);
            unset($this->sendAuth);
            unset($this->auth);
        }
        if(VARS::get("SEND_PRGID")){
            $this->getPrgId = VARS::get("SEND_PRGID");
            $this->sendAuth = VARS::get("SEND_AUTH");
        }
        $this->auth = $this->getPrgId ? $this->sendAuth : AUTHORITY;
        $this->cmd = VARS::request("cmd");
        common::GetControlMaster_Fnc($this->control);

        //プロパティファイルの読込み
        $this->getPropertiesAll();

    }
}
?>
