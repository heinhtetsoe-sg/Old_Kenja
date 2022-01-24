<?php

require_once('for_php7.php');

require_once('knjl510aModel.inc');
require_once('knjl510aQuery.inc');

class knjl510aController extends Controller {
    var $ModelClassName = "knjl510aModel";
    var $ProgramID      = "KNJL510A";

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
                    $this->callView("knjl510aForm1");
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
                    $this->callView("knjl510aForm1");
                    break 2;
                case "delete":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getDeleteModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "reset":
                case "reload1":
                case "":
                    $sessionInstance->setCmd("main");
                    break 1;
                case "sendFsCd":
                    $sessionInstance->getSendModel();
                    return;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$KNJL510ACtl = new knjl510aController;
?>
