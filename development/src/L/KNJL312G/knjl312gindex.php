<?php

require_once('for_php7.php');

require_once('knjl312gModel.inc');
require_once('knjl312gQuery.inc');

class knjl312gController extends Controller {
    var $ModelClassName = "knjl312gModel";
    var $ProgramID      = "KNJL312G";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl312g":
                    $sessionInstance->knjl312gModel();
                    $this->callView("knjl312gForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl312gCtl = new knjl312gController;
?>
