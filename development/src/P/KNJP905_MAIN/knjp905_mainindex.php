<?php

require_once('for_php7.php');

require_once('knjp905_mainModel.inc');
require_once('knjp905_mainQuery.inc');

class knjp905_mainController extends Controller {
    var $ModelClassName = "knjp905_mainModel";
    var $ProgramID      = "KNJP905_MAIN";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "torikomi":
                case "edit":
                case "main":
                case "cancel":
                    $this->callView("knjp905_mainForm1");
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
                case "line_copy":
                    $sessionInstance->getCopyModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "main_remark":
                case "cancel_remark":
                case "tesuryo_bikou":
                    $this->callView("knjp905_mainSubForm1");
                    break 2;
                case "update_remark":
                    $sessionInstance->getUpdateModelTesuRyoRemark();
                    $sessionInstance->setCmd("main_remark");
                    break 1;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjp905_mainCtl = new knjp905_mainController;
//var_dump($_REQUEST);
?>
