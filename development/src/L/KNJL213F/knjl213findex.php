<?php

require_once('for_php7.php');

require_once('knjl213fModel.inc');
require_once('knjl213fQuery.inc');

class knjl213fController extends Controller {
    var $ModelClassName = "knjl213fModel";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl213f":
                    $sessionInstance->knjl213fModel();
                    $this->callView("knjl213fForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl213fCtl = new knjl213fController;
var_dump($_REQUEST);
?>
