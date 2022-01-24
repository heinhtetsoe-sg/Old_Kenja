<?php

require_once('for_php7.php');

require_once('knjmp930_mainModel.inc');
require_once('knjmp930_mainQuery.inc');

class knjmp930_mainController extends Controller {
    var $ModelClassName = "knjmp930_mainModel";
    var $ProgramID      = "KNJMP930_MAIN";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "edit":
                case "main":
                case "cancel":
                    $this->callView("knjmp930_mainForm1");
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
$knjmp930_mainCtl = new knjmp930_mainController;
//var_dump($_REQUEST);
?>
