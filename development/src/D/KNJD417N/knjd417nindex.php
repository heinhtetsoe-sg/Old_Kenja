<?php

require_once('for_php7.php');

require_once('knjd417nModel.inc');
require_once('knjd417nQuery.inc');

class knjd417nController extends Controller {
    var $ModelClassName = "knjd417nModel";
    var $ProgramID      = "KNJD417N";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "update":
                     $sessionInstance->setAccessLogDetail("U", $ProgramID);
                     $sessionInstance->getUpdateModel();
                     //変更済みの場合は詳細画面に戻る
                     break 1;
                case "delete":
                     $sessionInstance->setAccessLogDetail("D", $ProgramID);
                     $sessionInstance->getDeleteModel();
                     //変更済みの場合は詳細画面に戻る
                     $sessionInstance->setCmd("clear");
                     break 1;
                case "copy":
                     $sessionInstance->setAccessLogDetail("U", $ProgramID);
                     $sessionInstance->getCopyModel();
                     //変更済みの場合は詳細画面に戻る
                     $sessionInstance->setCmd("main");
                     break 1;
                case "error":
                     $this->callView("error");
                     break 2;
                case "capture":
                case "level":
                     $sessionInstance->setAccessLogDetail("S", $ProgramID);
                     $this->callView("knjd417nForm1");
                     break 2;
                case "clear":
                case "change":
                case "change2":
                case "main":
                case "default":
                case "check":
                case "set":
                case "":
                     $sessionInstance->setAccessLogDetail("S", $ProgramID);
                     $this->callView("knjd417nForm1");
                     break 2;
                default:
                     $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                     $this->callView("error");
                     break 2;
            }
        }
    }
}
$knjd417nCtl = new knjd417nController;
?>
