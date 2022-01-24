<?php

require_once('for_php7.php');

require_once('knja117Model.inc');
require_once('knja117Query.inc');

class knja117Controller extends Controller {
    var $ModelClassName = "knja117Model";
    var $ProgramID      = "KNJA117";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "edit":
                case "change":
                case "reset":
                    $this->callView("knja117Form1");
                    break 2;
                case "update":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knja117Ctl = new knja117Controller;
?>
