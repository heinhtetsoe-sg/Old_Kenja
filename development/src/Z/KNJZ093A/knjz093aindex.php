<?php

require_once('for_php7.php');

require_once('knjz093aModel.inc');
require_once('knjz093aQuery.inc');

class knjz093aController extends Controller {
    var $ModelClassName = "knjz093aModel";
    var $ProgramID      = "KNJZ093A";

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
                case "":
                case "main";
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjz093aForm1");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjz093aCtl = new knjz093aController;
?>
