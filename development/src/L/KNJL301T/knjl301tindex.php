<?php

require_once('for_php7.php');

require_once('knjl301tModel.inc');
require_once('knjl301tQuery.inc');

class knjl301tController extends Controller {
    var $ModelClassName = "knjl301tModel";
    var $ProgramID      = "KNJL301T";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl301t":
                    $sessionInstance->knjl301tModel();
                    $this->callView("knjl301tForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl301tCtl = new knjl301tController;
?>
