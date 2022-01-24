<?php

require_once('for_php7.php');

require_once('knjz291Model.inc');
require_once('knjz291Query.inc');

class knjz291Controller extends Controller {
    var $ModelClassName = "knjz291Model";
    var $ProgramID      = "KNJZ291";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "new":
                case "search":
                case "search_ken":
                case "edit":
                case "change":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjz291Form2");
                    break 2;
                case "add":
                    $sessionInstance->setAccessLogDetail("I", $ProgramID);
                    $sessionInstance->getInsertModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "update":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "reset":
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "list":
                case "chg_year":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjz291Form1");
                    break 2;
                case "delete":
                    $sessionInstance->setAccessLogDetail("D", $ProgramID);
                    $sessionInstance->getDeleteModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "subform1":
                case "subform1_clear":
                case "list2":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjz291SubForm1");
                    break 2;
                case "subform1_add":
                    $sessionInstance->setAccessLogDetail("I", $ProgramID);
                    $sessionInstance->getInsertSubformModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("subform1");
                    break 1;
                case "subform1_update":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $sessionInstance->getUpdateSubformModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("subform1");
                    break 1;
                case "subform1_delete":
                    $sessionInstance->setAccessLogDetail("D", $ProgramID);
                    $sessionInstance->getDeleteSubformModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("subform1");
                    break 1;
                case "copy":
                    $sessionInstance->getCopyModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "execute":
                    $sessionInstance->setAccessLogDetail("E", $ProgramID);
                    $sessionInstance->getExecModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "":
                    //分割フレーム作成
                    $args["left_src"] = "knjz291index.php?cmd=list";
                    $args["right_src"] = "knjz291index.php?cmd=edit";
                    $args["cols"] = "25%,75%";
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
$knjz291Ctl = new knjz291Controller;
?>
