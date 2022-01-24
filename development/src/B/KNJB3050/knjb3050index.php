<?php

require_once('for_php7.php');

require_once('knjb3050Model.inc');
require_once('knjb3050Query.inc');

class knjb3050Controller extends Controller {
    var $ModelClassName = "knjb3050Model";
    var $ProgramID      = "KNJB3050";

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
                    $this->callView("knjb3050SubForm2");
                    break 2;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                case "main":
                case "clear";
                    $this->callView("knjb3050Form1");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjb3050Ctl = new knjb3050Controller;
?>
