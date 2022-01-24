<?php

require_once('for_php7.php');

require_once('knjl308rModel.inc');
require_once('knjl308rQuery.inc');

class knjl308rController extends Controller {
    var $ModelClassName = "knjl308rModel";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl308r":
                    $sessionInstance->knjl308rModel();
                    $this->callView("knjl308rForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knjl308rCtl = new knjl308rController;
var_dump($_REQUEST);
?>
