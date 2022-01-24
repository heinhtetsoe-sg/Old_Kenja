<?php

require_once('for_php7.php');

require_once('knjmp920_mainModel.inc');
require_once('knjmp920_mainQuery.inc');

class knjmp920_mainController extends Controller {
    var $ModelClassName = "knjmp920_mainModel";
    var $ProgramID      = "KNJMP920_MAIN";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "edit":
                case "main":
                case "cancel":
                    $this->callView("knjmp920_mainForm1");
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
$knjmp920_mainCtl = new knjmp920_mainController;
//var_dump($_REQUEST);
?>
