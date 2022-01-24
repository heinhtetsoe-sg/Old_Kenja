<?php

require_once('for_php7.php');

require_once('knjx_iinkaiModel.inc');
require_once('knjx_iinkaiQuery.inc');

class knjx_iinkaiController extends Controller {
    var $ModelClassName = "knjx_iinkaiModel";
    var $ProgramID      = "KNJX_IINKAI";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "edit":
                    $this->callView("knjx_iinkaiForm1");
                    break 2;
                case "end":
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    $sessionInstance->knjx_iinkaiModel();
                    $this->callView("knjx_iinkaiForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjx_iinkaiCtl = new knjx_iinkaiController;
?>
