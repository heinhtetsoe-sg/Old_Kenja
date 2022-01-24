<?php

require_once('for_php7.php');

require_once('knjp746Model.inc');
require_once('knjp746Query.inc');

class knjp746Controller extends Controller {
    var $ModelClassName = "knjp746Model";
    var $ProgramID      = "KNJP746";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "main":
                    $this->callView("knjp746Form1");
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
$knjp746Ctl = new knjp746Controller;
//var_dump($_REQUEST);
?>
