<?php

require_once('for_php7.php');

require_once('knjp737Model.inc');
require_once('knjp737Query.inc');

class knjp737Controller extends Controller {
    var $ModelClassName = "knjp737Model";
    var $ProgramID      = "KNJP737";

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
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getCopyModel();
                    $sessionInstance->setCmd("main");
                    break 1;
               case "error":
                    $this->callView("error");
                    break 2;
               case "reflect":
               case "check":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjp737Form1");
                    break 2;
               //CSV取込
               case "exec":
                    $sessionInstance->getExecModel();
                    $sessionInstance->setCmd("reflect");
                    break 1;
               case "clear":
               case "change":
               case "main":
               case "":
                    $this->callView("knjp737Form1");
                    break 2;
               default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjp737Ctl = new knjp737Controller;
?>
