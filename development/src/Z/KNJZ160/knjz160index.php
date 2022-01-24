<?php

require_once('for_php7.php');

require_once('knjz160Model.inc');
require_once('knjz160Query.inc');

class knjz160Controller extends Controller {
    var $ModelClassName = "knjz160Model";
    var $ProgramID      = "KNJZ160";

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
                    $sessionInstance->setCmd("sel");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                case "sel";
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjz160Form1");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
            
        }
    }
}
$knjz160Ctl = new knjz160Controller;
//var_dump($_REQUEST);
?>
