<?php

require_once('for_php7.php');

require_once('knjz235vModel.inc');
require_once('knjz235vQuery.inc');

class knjz235vController extends Controller {
    var $ModelClassName = "knjz235vModel";
    var $ProgramID      = "KNJZ235V";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "knjz235v":
                case "grade":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $sessionInstance->knjz235vModel();
                    $this->callView("knjz235vForm1");
                    exit;
                case "update":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("read");
                    break 1;
                case "copy":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getCopyModel();
                    $sessionInstance->setCmd("read");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                case "read";
                case "clear";
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjz235vForm1");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjz235vCtl = new knjz235vController;
?>
