<?php

require_once('for_php7.php');

require_once('knjp920_mainModel.inc');
require_once('knjp920_mainQuery.inc');

class knjp920_mainController extends Controller {
    var $ModelClassName = "knjp920_mainModel";
    var $ProgramID      = "KNJP920_MAIN";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "edit":
                case "main":
                case "cancel":
                    $this->callView("knjp920_mainForm1");
                    break 2;
                case "delete_update":
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
$knjp920_mainCtl = new knjp920_mainController;
//var_dump($_REQUEST);
?>
