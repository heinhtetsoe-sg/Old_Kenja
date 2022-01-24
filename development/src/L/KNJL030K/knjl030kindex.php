<?php

require_once('for_php7.php');

require_once('knjl030kModel.inc');
require_once('knjl030kQuery.inc');

class knjl030kController extends Controller {
    var $ModelClassName = "knjl030kModel";
    var $ProgramID      = "KNJL030K";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "reference":
                case "reload2":
                case "back1":
                case "next1":
                    $this->callView("knjl030kForm1");
                    break 2;
                case "add":
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getInsertModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "update":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "back":
                case "next":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getUpdateModel();
                    $this->callView("knjl030kForm1");
                    break 2;
#                case "next":
#                    $this->checkAuth(DEF_UPDATE_RESTRICT);
#                    $sessionInstance->getUpdateModel();
#                    $this->callView("knjl030kForm1");
#                    break 2;
                case "delete":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getDeleteModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
#                case "reference":
#                    $this->callView("knjl030kForm1");
#                    break 2;
#                case "reload2":
#                    $this->callView("knjl030kForm1");
#                    break 2;
                case "reset":
                case "reload1":
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
$KNJL030KCtl = new knjl030kController;
?>
