<?php

require_once('for_php7.php');

require_once('knjz221Model.inc');
require_once('knjz221Query.inc');

class knjz221Controller extends Controller {
    var $ModelClassName = "knjz221Model";
    var $ProgramID      = "KNJZ221";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "copy":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $sessionInstance->getCopyModel();
                    $sessionInstance->setCmd("list");
                    break 1;
                case "reset":
                case "edit":
                    $this->callView("knjz221Form2");
                    break 2;
                case "update":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "list":
                    $this->callView("knjz221Form1");
                    break 2;
                case "end":
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    //分割フレーム作成
                    $args["left_src"]   = "knjz221index.php?cmd=list";
                    $args["right_src"]  = "knjz221index.php?cmd=edit";
                    $args["cols"] = "60%,40%";
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
$knjz221Ctl = new knjz221Controller;
?>
