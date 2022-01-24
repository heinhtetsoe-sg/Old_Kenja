<?php

require_once('for_php7.php');

require_once('knjf305Model.inc');
require_once('knjf305Query.inc');

class knjf305Controller extends Controller {
    var $ModelClassName = "knjf305Model";
    var $ProgramID      = "KNJF305";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "change":
                case "main":
                case "reset":
                    $this->callView("knjf305Form1");
                    break 2;
                case "houkoku":
                    $sessionInstance->getUpdateEdboardModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("main");
                    break 1;
                case "update":
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
$knjf305Ctl = new knjf305Controller;
?>
