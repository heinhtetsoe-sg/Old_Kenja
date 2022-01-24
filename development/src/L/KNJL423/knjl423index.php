<?php

require_once('for_php7.php');

require_once('knjl423Model.inc');
require_once('knjl423Query.inc');

class knjl423Controller extends Controller {
    var $ModelClassName = "knjl423Model";
    var $ProgramID      = "KNJL423";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl423":
                case "print":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $sessionInstance->knjl423Model();
                    $this->callView("knjl423Form1");
                    exit;
                case "add":
                    $sessionInstance->setAccessLogDetail("I", $ProgramID);
                    $sessionInstance->getInsertModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("print");
                    break 1;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl423Ctl = new knjl423Controller;
//var_dump($_REQUEST);
?>
