<?php

require_once('for_php7.php');

require_once('knjd155Model.inc');
require_once('knjd155Query.inc');

class knjd155Controller extends Controller {
    var $ModelClassName = "knjd155Model";
    var $ProgramID      = "KNJD155";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "reset":
                case "back":
                    $this->callView("knjd155Form1");
                    break 2;
                case "update":
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("main");
                    break 1;
                case "replace":
                    $this->callView("knjd155SubForm1");
                    break 2;
                case "replace_update":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getReplaceModel();
                    $sessionInstance->setCmd("replace");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    $sessionInstance->setCmd("main");
                    break 1;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd155Ctl = new knjd155Controller;
?>
