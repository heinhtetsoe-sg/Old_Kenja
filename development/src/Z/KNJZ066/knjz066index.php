<?php

require_once('for_php7.php');

require_once('knjz066Model.inc');
require_once('knjz066Query.inc');

class knjz066Controller extends Controller {
    var $ModelClassName = "knjz066Model";
    var $ProgramID      = "KNJZ066";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
               case "update":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    break 1;
               case "delete":
                    $sessionInstance->setAccessLogDetail("D", $ProgramID);
                    $sessionInstance->getDeleteModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("main");
                    break 1;
               case "copy":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $sessionInstance->getCopyModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("main");
                    break 1;
               case "error":
                    $this->callView("error");
                    break 2;
               case "level":
               case "check":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjz066Form1");
                    break 2;
               case "clear":
               case "change":
               case "main":
               case "default":
               case "":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjz066Form1");
                    break 2;
               default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjz066Ctl = new knjz066Controller;
?>
