<?php

require_once('for_php7.php');

require_once('knjd181lModel.inc');
require_once('knjd181lQuery.inc');

class knjd181lController extends Controller {
    var $ModelClassName = "knjd181lModel";
    var $ProgramID      = "KNJD181L";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd181l":
                    $sessionInstance->knjd181lModel();
                    $this->callView("knjd181lForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd181lCtl = new knjd181lController;
?>
