<?php

require_once('for_php7.php');

require_once('knjz239Model.inc');
require_once('knjz239Query.inc');

class knjz239Controller extends Controller {
    var $ModelClassName = "knjz239Model";
    var $ProgramID      = "KNJZ239";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "clear":
                case "edit":
                case "set":
                case "reset":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjz239Form2");
                    break 2;
                case "add":
                    $sessionInstance->setAccessLogDetail("I", $ProgramID);
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getInsertModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "update":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "copy":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getInsertCopyModel();
                    $sessionInstance->setCmd("list");
                    break 1;
                case "list":
                case "combo":
                case "list_from_right":
                    $this->callView("knjz239Form1");
                    break 2;
                case "delete":
                    $sessionInstance->setAccessLogDetail("D", $ProgramID);
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getDeleteModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    //分割フレーム作成
                    $args["left_src"] = "knjz239index.php?cmd=list";
                    $args["right_src"] = "knjz239index.php?cmd=edit";
                    $args["cols"] = "42%,*";
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
$knjz239Ctl = new knjz239Controller;
//var_dump($_REQUEST);
?>
