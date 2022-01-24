<?php

require_once('for_php7.php');

require_once('knjz095kModel.inc');
require_once('knjz095kQuery.inc');

class knjz095kController extends Controller {
    var $ModelClassName = "knjz095kModel";
    var $ProgramID      = "knjz095k";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "prefecturescd":
                case "edit":
                case "reset":
                    $this->callView("knjz095kForm2");
                    break 2;
                case "changeYear":
                    $this->callView("knjz095kForm1");
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
                    $this->callView("knjz095kForm1");
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
                    $args["left_src"] = "knjz095kindex.php?cmd=list";
                    $args["right_src"] = "knjz095kindex.php?cmd=edit";
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
$knjz095kCtl = new knjz095kController;
//var_dump($_REQUEST);
?>
