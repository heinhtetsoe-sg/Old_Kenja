<?php

require_once('for_php7.php');

require_once('knjl034nModel.inc');
require_once('knjl034nQuery.inc');

class knjl034nController extends Controller {
    var $ModelClassName = "knjl034nModel";
    var $ProgramID      = "KNJL034N";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "main":
                case "select":
                case "knjl034n":
                    $sessionInstance->knjl034nModel();
                    $this->callView("knjl034nForm1");
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
$knjl034nCtl = new knjl034nController;
var_dump($_REQUEST);
?>
