<?php

require_once('for_php7.php');

require_once('knjl015rModel.inc');
require_once('knjl015rQuery.inc');

class knjl015rController extends Controller {
    var $ModelClassName = "knjl015rModel";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl015r":
                    $sessionInstance->knjl015rModel();
                    $this->callView("knjl015rForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knjl015rCtl = new knjl015rController;
var_dump($_REQUEST);
?>
