<?php

require_once('for_php7.php');

require_once('knjz178Model.inc');
require_once('knjz178Query.inc');

class knjz178Controller extends Controller {
    var $ModelClassName = "knjz178Model";
    var $ProgramID          = "KNJZ178";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
               case "update":
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("main");
                    break 1;
               case "error":
                    $this->callView("error");
                    break 2;
               case "level":
                    $this->callView("knjz178Form1");
                    break 2;
               case "clear":
               case "change":
               case "main":
               case "default":
               case "":
                    $this->callView("knjz178Form1");
                    break 2;
               default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjz178Ctl = new knjz178Controller;
//var_dump($_REQUEST);
?>
