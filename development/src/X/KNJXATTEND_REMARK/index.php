<?php

require_once('for_php7.php');

require_once('knjxattend_remarkModel.inc');
require_once('knjxattend_remarkQuery.inc');

class knjxattend_remarkController extends Controller {
    var $ModelClassName = "knjxattend_remarkModel";
    var $ProgramID      = "knjxattend_remark";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                    $this->callView("knjxattend_remarkForm1");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjxattend_remarkCtl = new knjxattend_remarkController;
?>
