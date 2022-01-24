<?php

require_once('for_php7.php');

require_once('knjl301sModel.inc');
require_once('knjl301sQuery.inc');

class knjl301sController extends Controller {
    var $ModelClassName = "knjl301sModel";
    var $ProgramID      = "KNJL301S";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl301s":
                    $sessionInstance->knjl301sModel();
                    $this->callView("knjl301sForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl301sCtl = new knjl301sController;
?>
