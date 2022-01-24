<?php

require_once('for_php7.php');

require_once('knjp914Model.inc');
require_once('knjp914Query.inc');

class knjp914Controller extends Controller {
    var $ModelClassName = "knjp914Model";
    var $ProgramID      = "KNJP914";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "main":
                    $this->callView("knjp914Form1");
                    break 2;
                case "update":
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("main");
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
$knjp914Ctl = new knjp914Controller;
//var_dump($_REQUEST);
?>
