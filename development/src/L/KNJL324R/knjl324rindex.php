<?php

require_once('for_php7.php');

require_once('knjl324rModel.inc');
require_once('knjl324rQuery.inc');

class knjl324rController extends Controller {
    var $ModelClassName = "knjl324rModel";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl324r":
                    $sessionInstance->knjl324rModel();
                    $this->callView("knjl324rForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knjl324rCtl = new knjl324rController;
var_dump($_REQUEST);
?>
