<?php

require_once('for_php7.php');

require_once('knjs580Model.inc');
require_once('knjs580Query.inc');

class knjs580Controller extends Controller {
    var $ModelClassName = "knjs580Model";
    var $ProgramID      = "KNJS580";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "change":
                case "change_class":
                case "reset":
                   $this->callView("knjs580Form1");
                   break 2;
                case "update":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("main");
                    break 1;
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

$knjs580Ctl = new knjs580Controller;
?>
