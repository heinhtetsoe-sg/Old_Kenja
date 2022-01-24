<?php

require_once('for_php7.php');

require_once('knjl413Model.inc');
require_once('knjl413Query.inc');

class knjl413Controller extends Controller {
    var $ModelClassName = "knjl413Model";
    var $ProgramID      = "KNJL413";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl413":
                case "print":
                    $sessionInstance->knjl413Model();
                    $this->callView("knjl413Form1");
                    exit;
                case "add":
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
$knjl413Ctl = new knjl413Controller;
//var_dump($_REQUEST);
?>
