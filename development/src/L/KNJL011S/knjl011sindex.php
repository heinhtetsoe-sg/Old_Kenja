<?php

require_once('for_php7.php');

require_once('knjl011sModel.inc');
require_once('knjl011sQuery.inc');

class knjl011sController extends Controller {
    var $ModelClassName = "knjl011sModel";
    var $ProgramID      = "KNJL011S";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "back1":
                case "next1":
                case "showdiv":
                case "showdivAdd":
                case "major":
                case "fin_type":
                    $this->callView("knjl011sForm1");
                    break 2;
                case "addnew":
                    $sessionInstance->getMaxExamno();
                    $sessionInstance->setCmd("showdivAdd");
                    break 1;
                case "add":
                    $sessionInstance->getInsertModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "update":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "back":
                    $sessionInstance->getUpdateModel();
                    $this->callView("knjl011sForm1");
                    break 2;
                case "next":
                    $sessionInstance->getUpdateModel();
                    $this->callView("knjl011sForm1");
                    break 2;
                case "delete":
                    $sessionInstance->getDeleteModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "reference":
                    $this->callView("knjl011sForm1");
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
$knjl011sCtl = new knjl011sController;
?>
