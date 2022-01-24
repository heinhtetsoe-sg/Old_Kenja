<?php

require_once('for_php7.php');

require_once('knjl074mModel.inc');
require_once('knjl074mQuery.inc');

class knjl074mController extends Controller {
    var $ModelClassName = "knjl074mModel";
    var $ProgramID      = "KNJL074M";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "chenge":
                case "reset":
                    $this->callView("knjl074mForm1");
                    break 2;
                case "update":
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 1;
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
$knjl074mCtl = new knjl074mController;
//var_dump($_REQUEST);
?>
