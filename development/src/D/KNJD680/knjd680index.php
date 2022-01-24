<?php

require_once('for_php7.php');

require_once('knjd680Model.inc');
require_once('knjd680Query.inc');

class knjd680Controller extends Controller {
    var $ModelClassName = "knjd680Model";
    var $ProgramID      = "KNJD680";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "reset":
                case "back":
                case "value_set":
                    $this->callView("knjd680Form1");
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
$knjd680Ctl = new knjd680Controller;
?>
