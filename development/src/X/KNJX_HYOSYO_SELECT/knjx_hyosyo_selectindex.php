<?php

require_once('for_php7.php');

require_once('knjx_hyosyo_selectModel.inc');
require_once('knjx_hyosyo_selectQuery.inc');

class knjx_hyosyo_selectController extends Controller {
    var $ModelClassName = "knjx_hyosyo_selectModel";
    var $ProgramID      = "KNJX_HYOSYO_SELECT";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "main":
                    $this->callView("knjx_hyosyo_selectForm1");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjx_hyosyo_selectCtl = new knjx_hyosyo_selectController;
?>
