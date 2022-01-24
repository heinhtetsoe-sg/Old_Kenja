<?php

require_once('for_php7.php');

require_once('knjx_tuuchisyoken_selectModel.inc');
require_once('knjx_tuuchisyoken_selectQuery.inc');

class knjx_tuuchisyoken_selectController extends Controller {
    var $ModelClassName = "knjx_tuuchisyoken_selectModel";
    var $ProgramID      = "KNJX_TUUCHISYOKEN_SELECT";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "main":
                    $this->callView("knjx_tuuchisyoken_selectForm1");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjx_tuuchisyoken_selectCtl = new knjx_tuuchisyoken_selectController;
?>
