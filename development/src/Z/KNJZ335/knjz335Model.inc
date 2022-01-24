<?php

require_once('for_php7.php');

class knjz335Model extends Model {
    var $cmd;
    var $leftStaffCd;        //primary
    var $selectdata;

    //メンバ変数の初期化
    function knjz335Model()
    {
    }

    function init()
    {   
        if (VARS::get("PROGRAMID")) {
            unset($this->leftStaffCd);
        }

        $this->getPropertiesAll(); //プロパティファイル

        $this->leftStaffCd = VARS::get("LEFT_STAFFCD") ? VARS::get("LEFT_STAFFCD") : $this->leftStaffCd;

        if ($this->cmd == "check"){
            $this->selectdata   = VARS::post("selectdata");
        }

        $this->cmd              = VARS::request("cmd");
    }

    function &getUpdateModel()
    {
        knjz335Query::getUpdateQuery($this);
        $this->setMessage("MSG201");

        return;
    }
}
?>
