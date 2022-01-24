<?php

require_once('for_php7.php');

require_once('knjl034bModel.inc');
require_once('knjl034bQuery.inc');

class knjl034bController extends Controller {
    var $ModelClassName = "knjl034bModel";
    var $ProgramID      = "KNJL034B";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "main":
                case "select":
                case "knjl034b":
                    $sessionInstance->knjl034bModel();
                    $this->callView("knjl034bForm1");
                    exit;
                case "update":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "copy":
                    $sessionInstance->getCopyYearModel();
                    $sessionInstance->setCmd("main");
                    break 1;
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
$knjl034bCtl = new knjl034bController;
var_dump($_REQUEST);
?>
