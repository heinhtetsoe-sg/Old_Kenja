<?php

require_once('for_php7.php');

require_once('knjp747Model.inc');
require_once('knjp747Query.inc');

class knjp747Controller extends Controller {
    var $ModelClassName = "knjp747Model";
    var $ProgramID      = "knjp747";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "bank":
                    $sessionInstance->knjp747Model();
                    $this->callView("knjp747Form2");
                    exit;
                case "edit":
                case "reset":
                    $this->callView("knjp747Form2");
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
                    $this->callView("knjp747Form1");
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
                    $args["left_src"] = "knjp747index.php?cmd=list";
                    $args["right_src"] = "knjp747index.php?cmd=edit";
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
$knjp747Ctl = new knjp747Controller;
//var_dump($_REQUEST);
?>
