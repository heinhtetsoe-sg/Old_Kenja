<?php

require_once('for_php7.php');

require_once('knjd153rModel.inc');
require_once('knjd153rQuery.inc');

class knjd153rController extends Controller {
    var $ModelClassName = "knjd153rModel";
    var $ProgramID      = "KNJD153R";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "reset":
                    $this->callView("knjd153rForm1");
                    break 2;
                case "main":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $this->callView("knjd153rForm1");
                    break 2;
                case "update":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID); 
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("main");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    $sessionInstance->setCmd("main");
                    break 1;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd153rCtl = new knjd153rController;
?>
