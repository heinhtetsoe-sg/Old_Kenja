<?php

require_once('for_php7.php');

require_once('knje370dModel.inc');
require_once('knje370dQuery.inc');

class knje370dController extends Controller {
    var $ModelClassName = "knje370dModel";
    var $ProgramID      = "KNJE370D";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knje370d":
                    $sessionInstance->knje370dModel();
                    $this->callView("knje370dForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knje370dCtl = new knje370dController;
?>
