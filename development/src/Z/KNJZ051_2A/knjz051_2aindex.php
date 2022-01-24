<?php

require_once('for_php7.php');

require_once('knjz051_2aModel.inc');
require_once('knjz051_2aQuery.inc');

class knjz051_2aController extends Controller {
    var $ModelClassName = "knjz051_2aModel";
    var $ProgramID      = "KNJZ051A";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "edit":
                case "reset":
                    $sessionInstance->setAccessLogDetail("S", "KNJZ051_2A");
                    $this->callView("knjz051_2aForm2");
                    break 2;
                case "add":
                    $sessionInstance->setAccessLogDetail("I", "KNJZ051_2A");
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "update":
                    $sessionInstance->setAccessLogDetail("U", "KNJZ051_2A");
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "list":
                    $sessionInstance->setAccessLogDetail("S", "KNJZ051_2A");
                    $this->callView("knjz051_2aForm1");
                    break 2;
                case "delete":
                    $sessionInstance->setAccessLogDetail("D", "KNJZ051_2A");
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getDeleteModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    //分割フレーム作成
                    $args["left_src"] = "knjz051_2aindex.php?cmd=list";
                    $args["right_src"] = "knjz051_2aindex.php?cmd=edit";
                    $args["cols"] = "50%,50%";
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
$knjz051_2aCtl = new knjz051_2aController;
?>
