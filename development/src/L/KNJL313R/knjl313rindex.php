<?php

require_once('for_php7.php');

require_once('knjl313rModel.inc');
require_once('knjl313rQuery.inc');

class knjl313rController extends Controller {
    var $ModelClassName = "knjl313rModel";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl313r":
                    $sessionInstance->knjl313rModel();
                    $this->callView("knjl313rForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knjl313rCtl = new knjl313rController;
var_dump($_REQUEST);
?>
