<?php

require_once('for_php7.php');

require_once('knjl323rModel.inc');
require_once('knjl323rQuery.inc');

class knjl323rController extends Controller {
    var $ModelClassName = "knjl323rModel";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl323r":
                    $sessionInstance->knjl323rModel();
                    $this->callView("knjl323rForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knjl323rCtl = new knjl323rController;
var_dump($_REQUEST);
?>
