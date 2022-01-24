<?php

require_once('for_php7.php');

require_once('knjl300rModel.inc');
require_once('knjl300rQuery.inc');

class knjl300rController extends Controller {
    var $ModelClassName = "knjl300rModel";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl300r":
                    $sessionInstance->knjl300rModel();
                    $this->callView("knjl300rForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knjl300rCtl = new knjl300rController;
var_dump($_REQUEST);
?>
