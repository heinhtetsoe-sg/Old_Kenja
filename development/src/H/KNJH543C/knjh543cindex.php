<?php

require_once('for_php7.php');

require_once('knjh543cModel.inc');
require_once('knjh543cQuery.inc');

class knjh543cController extends Controller {
    var $ModelClassName = "knjh543cModel";
    var $ProgramID      = "KNJH543C";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "sel":
                case "change":
                case "clear":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjh543cForm2");
                    break 2;
                case "update":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("sel");
                    break 1;
                case "updateWeight":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getUpdateModelWeight();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("sel");
                    break 1;
                case "list":
                case "list_update":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjh543cForm1");
                    break 2;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    //分割フレーム作成
                    $args["left_src"]   = "knjh543cindex.php?cmd=list";
                    $args["right_src"]  = "knjh543cindex.php?cmd=sel";
                    $args["cols"] = "45%,*";
                    View::frame($args);
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjh543cCtl = new knjh543cController;
?>
