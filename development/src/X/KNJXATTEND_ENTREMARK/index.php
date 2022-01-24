<?php

require_once('for_php7.php');

require_once('knjxattend_entremarkModel.inc');
require_once('knjxattend_entremarkQuery.inc');

class knjxattend_entremarkController extends Controller {
    var $ModelClassName = "knjxattend_entremarkModel";
    var $ProgramID      = "knjxattend_entremark";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                    $this->callView("knjxattend_entremarkForm1");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjxattend_entremarkCtl = new knjxattend_entremarkController;
?>
