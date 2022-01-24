<?php

require_once('for_php7.php');

require_once('knjl321rModel.inc');
require_once('knjl321rQuery.inc');

class knjl321rController extends Controller {
    var $ModelClassName = "knjl321rModel";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl321r":
                    $sessionInstance->knjl321rModel();
                    $this->callView("knjl321rForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knjl321rCtl = new knjl321rController;
var_dump($_REQUEST);
?>
