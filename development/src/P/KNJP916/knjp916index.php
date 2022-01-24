<?php

require_once('for_php7.php');

require_once('knjp916Model.inc');
require_once('knjp916Query.inc');

class knjp916Controller extends Controller {
    var $ModelClassName = "knjp916Model";
    var $ProgramID      = "KNJP916";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "main":
                    $this->callView("knjp916Form1");
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
$knjp916Ctl = new knjp916Controller;
//var_dump($_REQUEST);
?>
