<?php

require_once('for_php7.php');

require_once('knjz291_swModel.inc');
require_once('knjz291_swQuery.inc');

class knjz291_swController extends Controller {
    var $ModelClassName = "knjz291_swModel";
    var $ProgramID      = "KNJZ291_SW";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "edit":
                case "change":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjz291_swForm2");
                    break 2;
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
                    $this->callView("knjz291_swForm1");
                    break 2;
                case "subform1":
                case "subform1_clear":
                case "list2":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjz291_swSubForm1");
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
                case "error":
                    $this->callView("error");
                    break 2;
                case "copy":
                    $sessionInstance->getCopyModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "":
                    //分割フレーム作成
                    $args["left_src"]  = "knjz291_swindex.php?cmd=list";
                    $args["right_src"] = "knjz291_swindex.php?cmd=edit";
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
$knjz291_swCtl = new knjz291_swController;
?>
