<?php

require_once('for_php7.php');

require_once('knjx_marathon_selectModel.inc');
require_once('knjx_marathon_selectQuery.inc');

class knjx_marathon_selectController extends Controller {
    var $ModelClassName = "knjx_marathon_selectModel";
    var $ProgramID      = "KNJX_MARATHON_SELECT";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "main":
                    $this->callView("knjx_marathon_selectForm1");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjx_marathon_selectCtl = new knjx_marathon_selectController;
?>
