<?php

require_once('for_php7.php');

require_once('knjx_bukatuModel.inc');
require_once('knjx_bukatuQuery.inc');

class knjx_bukatuController extends Controller {
    var $ModelClassName = "knjx_bukatuModel";
    var $ProgramID      = "KNJX_BUKATU";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "edit":
                    $this->callView("knjx_bukatuForm1");
                    break 2;
                case "end":
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    $sessionInstance->knjx_bukatuModel();
                    $this->callView("knjx_bukatuForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjx_bukatuCtl = new knjx_bukatuController;
?>
