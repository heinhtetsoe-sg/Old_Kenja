<?php

require_once('for_php7.php');

require_once('knjp940_mainModel.inc');
require_once('knjp940_mainQuery.inc');

class knjp940_mainController extends Controller {
    var $ModelClassName = "knjp940_mainModel";
    var $ProgramID      = "KNJP940_MAIN";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "edit":
                case "main":
                case "cancel":
                    $this->callView("knjp940_mainForm1");
                    break 2;
                case "update":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "delete":
                    $sessionInstance->getDeleteModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjp940_mainCtl = new knjp940_mainController;
//var_dump($_REQUEST);
?>
