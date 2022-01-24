<?php

require_once('for_php7.php');

require_once('knje453Model.inc');
require_once('knje453Query.inc');

class knje453Controller extends Controller {
    var $ModelClassName = "knje453Model";
    var $ProgramID      = "KNJE453";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
               case "update":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("main");
                    break 1;
               case "error":
                    $this->callView("error");
                    break 2;
               case "clear":
               case "main":
               case "":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knje453Form1");
                    break 2;
               case "copy":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getCopyModel();
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
$knje453Ctl = new knje453Controller;
?>
