<?php

require_once('for_php7.php');

require_once('knjl016cModel.inc');
require_once('knjl016cQuery.inc');

class knjl016cController extends Controller {
    var $ModelClassName = "knjl016cModel";
    var $ProgramID      = "KNJL016C";

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
                case "testdiv":
                case "shdiv":
                    $this->callView("knjl016cForm1");
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
                    $this->callView("knjl016cForm1");
                    break 2;
                case "next":
                    $sessionInstance->getUpdateModel();
                    $this->callView("knjl016cForm1");
                    break 2;
                case "delete":
                    $sessionInstance->getDeleteModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "reference":
                    $this->callView("knjl016cForm1");
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
$knjl016cCtl = new knjl016cController;
?>
