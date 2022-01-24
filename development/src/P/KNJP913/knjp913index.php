<?php

require_once('for_php7.php');

require_once('knjp913Model.inc');
require_once('knjp913Query.inc');

class knjp913Controller extends Controller {
    var $ModelClassName = "knjp913Model";
    var $ProgramID      = "KNJP913";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "cmdStart":
                case "main":
                case "reset":
                    $this->callView("knjp913Form1");
                    break 2;
                case "update":
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("main");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    $sessionInstance->setCmd("cmdStart");
                    break 1;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjp913Ctl = new knjp913Controller;
?>
