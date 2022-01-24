<?php

require_once('for_php7.php');

require_once('knjl306rModel.inc');
require_once('knjl306rQuery.inc');

class knjl306rController extends Controller {
    var $ModelClassName = "knjl306rModel";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl306r":
                    $sessionInstance->knjl306rModel();
                    $this->callView("knjl306rForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl306rCtl = new knjl306rController;
?>
