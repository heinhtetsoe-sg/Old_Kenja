<?php

require_once('for_php7.php');

require_once('knjl212rModel.inc');
require_once('knjl212rQuery.inc');

class knjl212rController extends Controller {
    var $ModelClassName = "knjl212rModel";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl212r":
                    $sessionInstance->knjl212rModel();
                    $this->callView("knjl212rForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
			}
        }
            
    }
}
$knjl212rCtl = new knjl212rController;
var_dump($_REQUEST);
?>
