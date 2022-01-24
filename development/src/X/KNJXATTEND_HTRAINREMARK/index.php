<?php

require_once('for_php7.php');

require_once('knjxattend_htrainremarkModel.inc');
require_once('knjxattend_htrainremarkQuery.inc');

class knjxattend_htrainremarkController extends Controller {
    var $ModelClassName = "knjxattend_htrainremarkModel";
    var $ProgramID      = "knjxattend_htrainremark";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                    $this->callView("knjxattend_htrainremarkForm1");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjxattend_htrainremarkCtl = new knjxattend_htrainremarkController;
?>
