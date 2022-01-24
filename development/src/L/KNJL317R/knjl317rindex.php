<?php

require_once('for_php7.php');

require_once('knjl317rModel.inc');
require_once('knjl317rQuery.inc');

class knjl317rController extends Controller {
    var $ModelClassName = "knjl317rModel";
    var $ProgramID      = "KNJL317R";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl317r":
                case "changeTest":
                    $sessionInstance->knjl317rModel();
                    $this->callView("knjl317rForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl317rCtl = new knjl317rController;
?>
