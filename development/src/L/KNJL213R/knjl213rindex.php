<?php

require_once('for_php7.php');

require_once('knjl213rModel.inc');
require_once('knjl213rQuery.inc');

class knjl213rController extends Controller {
    var $ModelClassName = "knjl213rModel";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl213r":
                    $sessionInstance->knjl213rModel();
                    $this->callView("knjl213rForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl213rCtl = new knjl213rController;
var_dump($_REQUEST);
?>
