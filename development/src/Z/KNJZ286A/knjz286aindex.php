<?php

require_once('for_php7.php');

require_once('knjz286aModel.inc');
require_once('knjz286aQuery.inc');

class KNJZ286AController extends Controller {
    var $ModelClassName = "knjz286aModel";
    var $ProgramID      = "KNJZ286A";

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
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjz286aForm1");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$KNJZ286ACtl = new KNJZ286AController;
?>
