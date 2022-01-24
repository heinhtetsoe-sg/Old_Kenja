<?php

require_once('for_php7.php');

require_once('knjl322rModel.inc');
require_once('knjl322rQuery.inc');

class knjl322rController extends Controller {
    var $ModelClassName = "knjl322rModel";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl322r":
                    $sessionInstance->knjl322rModel();
                    $this->callView("knjl322rForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knjl322rCtl = new knjl322rController;
var_dump($_REQUEST);
?>
