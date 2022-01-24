<?php

require_once('for_php7.php');

require_once('knjz380qModel.inc');
require_once('knjz380qQuery.inc');

class knjz380qController extends Controller {
    var $ModelClassName = "knjz380qModel";
    var $ProgramID      = "KNJZ380Q";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "knjz380qForm2":
                case "edit":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjz380qForm2");
                    break 2;
                case "add":
                    $sessionInstance->setAccessLogDetail("I", $ProgramID);
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getInsertModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "update":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "reset":
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "list":
                case "change":
                case "changeCm":
                    $this->callView("knjz380qForm1");
                    break 2;
                case "delete":
                    $sessionInstance->setAccessLogDetail("D", $ProgramID);
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getDeleteModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "copy":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getCopyModel();
                    $sessionInstance->setCmd("list");
                    break 1;
                case "copy2":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getCopyModel2();
                    $sessionInstance->setCmd("list");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    //分割フレーム作成
                    $args["left_src"] = "knjz380qindex.php?cmd=change";
                    $args["right_src"] = "knjz380qindex.php?cmd=edit";
                    $args["cols"] = "55%,45%";
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
$knjz380qCtl = new knjz380qController;
//var_dump($_REQUEST);
?>
