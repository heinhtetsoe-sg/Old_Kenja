<?php

require_once('for_php7.php');

require_once('knjb3056Model.inc');
require_once('knjb3056Query.inc');

class knjb3056Controller extends Controller {
    var $ModelClassName = "knjb3056Model";
    var $ProgramID      = "KNJB3056";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "update":
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("main");
                    break 1;
                case "chairToChair":
                    $sessionInstance->getUpdateChairModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("main");
                    break 1;
                case "retrunHR":
                    $sessionInstance->getUpdateHrModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("main");
                    break 1;
                case "subForm2":
                    $this->callView("knjb3056SubForm2");
                    break 2;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                case "main":
                case "clear";
                    $this->callView("knjb3056Form1");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjb3056Ctl = new knjb3056Controller;
?>
