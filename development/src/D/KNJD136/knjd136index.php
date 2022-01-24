<?php

require_once('for_php7.php');

require_once('knjd136Model.inc');
require_once('knjd136Query.inc');

class knjd136Controller extends Controller {
    var $ModelClassName = "knjd136Model";
    var $ProgramID      = "KNJD136";
    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "edit":
                    $this->callView("knjd136Form2");
                    break 2;
                case "add":
                case "update":
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getInsertModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "reset":
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "list":
                case "coursename":
                    $this->callView("knjd136Form1");
                    break 2;
                case "delete":
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getDeleteModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "copy":
                    $sessionInstance->getCopyModel();
                    $sessionInstance->setCmd("coursename");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    //分割フレーム作成
                    $args["left_src"] = "knjd136index.php?cmd=list";
                    $args["right_src"] = "knjd136index.php?cmd=edit";
                    $args["cols"] = "30%,70%";
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
$knjd136Ctl = new knjd136Controller;
//var_dump($_REQUEST);
?>
