<?php

require_once('for_php7.php');

require_once('knjz090kModel.inc');
require_once('knjz090kQuery.inc');

class knjz090kController extends Controller {
    var $ModelClassName = "knjz090kModel";
    var $ProgramID      = "knjz090k";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "prefecturescd":
                case "edit":
                case "reset":
                    $this->callView("knjz090kForm2");
                    break 2;
                case "changeYear":
                    $this->callView("knjz090kForm1");
                    break 2;
                case "add":
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getInsertModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "update":
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "copy":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getCopyModel();
                    $sessionInstance->setCmd("list");
                    break 1;
                case "list":
                    $this->callView("knjz090kForm1");
                    break 2;
                case "delete":
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getDeleteModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    //分割フレーム作成
                    $args["left_src"] = "knjz090kindex.php?cmd=list";
                    $args["right_src"] = "knjz090kindex.php?cmd=edit";
                    $args["cols"] = "54%,*";
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
$knjz090kCtl = new knjz090kController;
//var_dump($_REQUEST);
?>
