<?php

require_once('for_php7.php');

require_once('knjl050mModel.inc');
require_once('knjl050mQuery.inc');

class knjl050mController extends Controller {
    var $ModelClassName = "knjl050mModel";
    var $ProgramID      = "KNJL050M";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "kakutei":
                case "main":
                case "back":
                case "next":
                    $this->callView("knjl050mForm1");
                    break 2;
                case "add":
                    $sessionInstance->getInsertModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "first_search":
                    $sessionInstance->getUpdateModel();
                    $this->callView("knjl050mForm1");
                    break 2;
                case "update":
                    $sessionInstance->getUpdateModel();
                    $this->callView("knjl050mForm1");
                    break 2;
                case "upBack":
                    $sessionInstance->getUpdateModel();
                    $this->callView("knjl050mForm1");
                    break 2;
                case "upNext":
                    $sessionInstance->getUpdateModel();
                    $this->callView("knjl050mForm1");
                    break 2;
                case "delete":
                    $sessionInstance->getDeleteModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "reference":
                    $this->callView("knjl050mForm1");
                    break 2;
                case "reset":                
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
$knjl050mCtl = new knjl050mController;
?>
