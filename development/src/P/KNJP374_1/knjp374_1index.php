<?php

require_once('for_php7.php');

require_once('knjp374_1Model.inc');
require_once('knjp374_1Query.inc');

class knjp374_1Controller extends Controller {
    var $ModelClassName = "knjp374_1Model";
    var $ProgramID      = "KNJP374_1";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "edit":
                case "reset":
                    $this->callView("knjp374_1Form2");
                    break 2;
                case "add":
                    $this->checkAuth(DEF_UPDATABLE,  "", $sessionInstance->auth);
                    $sessionInstance->getInsertModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "update":
                    $this->checkAuth(DEF_UPDATABLE, "", $sessionInstance->auth);
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "copy":
                    $this->checkAuth(DEF_UPDATE_RESTRICT,  "", $sessionInstance->auth);
                    $sessionInstance->getCopyModel();
                    $sessionInstance->setCmd("list");
                    break 1;
                case "list":
                case "changeKind":
                    $this->callView("knjp374_1Form1");
                    break 2;
                case "delete":
                    $this->checkAuth(DEF_UPDATABLE,  "", $sessionInstance->auth);
                    $sessionInstance->getDeleteModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    //分割フレーム作成
                    $args["left_src"] = "knjp374_1index.php?cmd=list";
                    $args["right_src"] = "knjp374_1index.php?cmd=edit";
                    $args["cols"] = "40%,*";
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
$knjp374_1Ctl = new knjp374_1Controller;
?>
