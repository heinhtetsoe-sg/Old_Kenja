<?php

require_once('for_php7.php');

require_once('knjz210Model.inc');
require_once('knjz210Query.inc');

class knjz210Controller extends Controller {
    var $ModelClassName = "knjz210Model";
    var $ProgramID          = "KNJZ210";

    function main()
    {
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
               case "level":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjz210Form1");
                    break 2;
               case "clear":
               case "change":
               case "main":
               case "default":
               case "":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjz210Form1");
                    break 2;
               default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
            
        }
    }
}
$knjz210Ctl = new knjz210Controller;
//var_dump($_REQUEST);
?>
