<?php

require_once('for_php7.php');

require_once('knjz200aModel.inc');
require_once('knjz200aQuery.inc');

class knjz200aController extends Controller {
    var $ModelClassName = "knjz200aModel";
    var $ProgramID      = "KNJZ200A";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "edit":
                case "edit2":
                case "reset":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjz200aForm2");
                    break 2;
                case "list":
                case "combo":
                    $this->callView("knjz200aForm1");
                    break 2;
                case "copy":
                    $sessionInstance->setAccessLogDetail("I", $ProgramID);
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getCopyModel();
                    $sessionInstance->setCmd("list");
                    break 1;
                case "add":
                    $sessionInstance->setAccessLogDetail("I", $ProgramID);
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getInsertModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "update":
                    $sessionInstance->setAccessLogDetail("I", $ProgramID);
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "delete":
                    $sessionInstance->setAccessLogDetail("D", $ProgramID);
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getDeleteModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "replace":
                case "replaceA":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjz200aSubForm1");
                    break 2;
                case "replace_update":     //一括処理
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->ReplaceModel();
                    $sessionInstance->setCmd("replaceA");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    //分割フレーム作成
                    $args["left_src"]  = "knjz200aindex.php?cmd=list";
                    $args["right_src"] = "knjz200aindex.php?cmd=edit";
                    $args["cols"] = "50%, 50%";
                    View::frame($args);
                    exit;
                case "back":
                    //分割フレーム作成
                    $args["left_src"]  = "knjz200aindex.php?cmd=list";
                    $args["right_src"] = "knjz200aindex.php?cmd=edit";
                    $args["cols"] = "50%,50%";
                    View::frame($args);
                    return;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjz200aCtl = new knjz200aController;
?>
