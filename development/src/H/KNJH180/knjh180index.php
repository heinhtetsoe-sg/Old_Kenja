<?php

require_once('for_php7.php');

require_once('knjh180Model.inc');
require_once('knjh180Query.inc');

class knjh180Controller extends Controller {
    var $ModelClassName = "knjh180Model";
    var $ProgramID      = "KNJH180";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "execute":
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getExecuteModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("main");
                    break 1;
                case "":
                case "main":
                    $sessionInstance->getMainModel();
                    $this->callView("knjh180Form1");
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
$knjh180Ctl = new knjh180Controller;
?>
