<?php

require_once('for_php7.php');

require_once('knjp917Model.inc');
require_once('knjp917Query.inc');

class knjp917Controller extends Controller {
    var $ModelClassName = "knjp917Model";
    var $ProgramID      = "KNJP917";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "list":
                    $this->callView("knjp917Search");
                    break 2;
                case "search":
                    $this->callView("knjp917Search");
                    break 2;
                case "cmdStart":
                case "edit":
                case "sendList":
                    $this->callView("knjp917Form1");
                    break 2;
                case "update":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID); 
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    //分割フレーム作成
                    $args["left_src"] .= "knjp917index.php?cmd=list";
                    $args["right_src"] = "knjp917index.php?cmd=cmdStart";

                    $args["cols"] = "27%,*";
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
$KNJP917Ctl = new knjp917Controller;
//var_dump($_REQUEST);
?>
