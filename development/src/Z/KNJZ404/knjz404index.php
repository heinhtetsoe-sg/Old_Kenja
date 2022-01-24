<?php

require_once('for_php7.php');

require_once('knjz404Model.inc');
require_once('knjz404Query.inc');

class KNJZ404Controller extends Controller {
    var $ModelClassName = "KNJZ404Model";
    var $ProgramID      = "KNJZ404";

    function main() {
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
                case "clear";
                    $this->callView("knjz404Form1");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$KNJZ404Ctl = new KNJZ404Controller;
?>
