<?php

require_once('for_php7.php');

require_once('knjp909Model.inc');
require_once('knjp909Query.inc');

class knjp909Controller extends Controller {
    var $ModelClassName = "knjp909Model";
    var $ProgramID      = "KNJP909";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "main":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $this->callView("knjp909Form1");
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
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjp909Ctl = new knjp909Controller;
//var_dump($_REQUEST);
?>
