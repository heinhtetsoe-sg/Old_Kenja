<?php

require_once('for_php7.php');

require_once('knjp710Model.inc');
require_once('knjp710Query.inc');

class knjp710Controller extends Controller {
    var $ModelClassName = "knjp710Model";
    var $ProgramID      = "KNJP710";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "prefecturescd":
                case "edit":
                case "reset":
                    $this->callView("knjp710Form2");
                    break 2;
                case "reductionTarget":
                case "changeYear":
                    $this->callView("knjp710Form1");
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
                case "changeKind":
                    $this->callView("knjp710Form1");
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
                    $args["left_src"] = "knjp710index.php?cmd=list";
                    $args["right_src"] = "knjp710index.php?cmd=edit";
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
$knjp710Ctl = new knjp710Controller;
//var_dump($_REQUEST);
?>
