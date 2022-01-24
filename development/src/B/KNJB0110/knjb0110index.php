<?php

require_once('for_php7.php');

require_once('knjb0110Model.inc');
require_once('knjb0110Query.inc');

class knjb0110Controller extends Controller {
    var $ModelClassName = "knjb0110Model";
    var $ProgramID      = "KNJB0110";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "reset":
                    $this->callView("knjb0110Form1");
                    break 2;
                case "update":
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("main");
                    break 1;
                case "select_staff":
                    $this->callView("knjb0110SubMaster");
                    break 2;
                    
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
$knjb0110Ctl = new knjb0110Controller;
?>
