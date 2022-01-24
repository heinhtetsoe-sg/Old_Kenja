<?php

require_once('for_php7.php');

require_once('knjz120Model.inc');
require_once('knjz120Query.inc');

class knjz120Controller extends Controller {
    var $ModelClassName = "knjz120Model";
    var $ProgramID      = "KNJZ120";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "execute":
                    $sessionInstance->setAccessLogDetail("EI", $ProgramID);
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getExecuteModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("main");
                    break 1;
                case "":
                case "main":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $sessionInstance->getMainModel();
                    $this->callView("knjz120Form1");
                    break 2;
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
$knjz120Ctl = new knjz120Controller;
?>
