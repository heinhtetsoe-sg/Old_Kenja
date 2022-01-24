<?php
require_once('knjx_batsu_selectModel.inc');
require_once('knjx_batsu_selectQuery.inc');

class knjx_batsu_selectController extends Controller {
    var $ModelClassName = "knjx_batsu_selectModel";
    var $ProgramID      = "KNJX_BATSU_SELECT";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "main":
                    $this->callView("knjx_batsu_selectForm1");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjx_batsu_selectCtl = new knjx_batsu_selectController;
?>
