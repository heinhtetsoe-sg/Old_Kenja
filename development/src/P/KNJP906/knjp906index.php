<?php

require_once('for_php7.php');

require_once('knjp906Model.inc');
require_once('knjp906Query.inc');

class knjp906Controller extends Controller {
    var $ModelClassName = "knjp906Model";
    var $ProgramID      = "KNJP906";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "cmdStart":
                case "main":
                case "reset":
                    $this->callView("knjp906Form1");
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
$knjp906Ctl = new knjp906Controller;
?>
