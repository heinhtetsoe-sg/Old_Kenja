<?php

require_once('for_php7.php');

require_once('knjs030Model.inc');
require_once('knjs030Query.inc');

class knjs030Controller extends Controller {
    var $ModelClassName = "knjs030Model";
    var $ProgramID      = "KNJS030";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "subEnd":
                case "change":
                case "change_class":
                case "reset":
                case "hrDel":
                case "staffDel":
                   $this->callView("knjs030Form1");
                   break 2;
                case "bunkatu":
                    $this->callView("knjs030Bunkatu");
                    break 2;
                case "update":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "bunUpd":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getBunkatuUpdateModel();
                    $this->callView("knjs030Bunkatu");
                    break 2;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    $sessionInstance->setCmd("main");
                    break 1;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}

$knjs030Ctl = new knjs030Controller;
?>
