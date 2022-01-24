<?php

require_once('for_php7.php');

require_once('knjz010Model.inc');
require_once('knjz010Query.inc');

class knjz010Controller extends Controller {
    var $ModelClassName = "knjz010Model";
    var $ProgramID      = "KNJZ010";
    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "clear":
                case "edit":
                case "change";
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjz010Form1");
                    break 2;
                case "update":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $sessionInstance->setCmd("edit");
//                    $this->callView("knjz010Form1");
                    break 1;
//                    return; 
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
            
        }
    }
}
$knjz010Ctl = new knjz010Controller;
//var_dump($_REQUEST);
?>
