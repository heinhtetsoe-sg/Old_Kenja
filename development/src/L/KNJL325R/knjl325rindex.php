<?php

require_once('for_php7.php');

require_once('knjl325rModel.inc');
require_once('knjl325rQuery.inc');

class knjl325rController extends Controller {
    var $ModelClassName = "knjl325rModel";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl325r":
                    $sessionInstance->knjl325rModel();
                    $this->callView("knjl325rForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knjl325rCtl = new knjl325rController;
var_dump($_REQUEST);
?>
