<?php

require_once('for_php7.php');

require_once('knjz181Model.inc');
require_once('knjz181Query.inc');

class knjz181Controller extends Controller {
    var $ModelClassName = "knjz181Model";
    var $ProgramID          = "KNJZ181";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
               case "update":
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("main");
                    break 1;
               case "copy":
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getCopyModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("main");
                    break 1;
               case "error":
                    $this->callView("error");
                    break 2;
               case "level":
                    $this->callView("knjz181Form1");
                    break 2;
               case "clear":
               case "change":
               case "main":
               case "changeYear":
               case "":
                    $this->callView("knjz181Form1");
                    break 2;
               default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
            
        }
    }
}
$knjz181Ctl = new knjz181Controller;
//var_dump($_REQUEST);
?>
