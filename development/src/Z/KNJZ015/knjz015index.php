<?php

require_once('for_php7.php');

require_once('knjz015Model.inc');
require_once('knjz015Query.inc');

class knjz015Controller extends Controller {
    var $ModelClassName = "knjz015Model";
    var $ProgramID      = "KNJZ015";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "edit":
                case "change":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjz015Form2");
                    break 2;
                case "update":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "delete":
                    $sessionInstance->setAccessLogDetail("D", $ProgramID);
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getDeleteModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "list":
                    $sessionInstance->getMainModel();
                    $this->callView("knjz015Form1");
                    break 2;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    //分割フレーム作成
                    $args["left_src"] = "knjz015index.php?cmd=list";
                    $args["right_src"] = "knjz015index.php?cmd=edit";
                    $args["cols"] = "40%,60%";
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
$knjz015Ctl = new knjz015Controller;
//var_dump($_REQUEST);
?>
