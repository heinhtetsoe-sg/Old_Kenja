<?php

require_once('for_php7.php');

require_once('knje014Model.inc');
require_once('knje014Query.inc');

class knje014Controller extends Controller {
    var $ModelClassName = "knje014Model";
    var $ProgramID      = "KNJE014";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "change_course":
                case "reset":
                    $this->callView("knje014Form1");
                    break 2;
                case "change_hrclass":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knje014Form1");
                    break 2;
                case "update":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("main");
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
$knje014Ctl = new knje014Controller;
?>
