<?php

require_once('for_php7.php');

require_once('knjz422Model.inc');
require_once('knjz422Query.inc');

class knjz422Controller extends Controller {
    var $ModelClassName = "knjz422Model";
    var $ProgramID      = "KNJZ422";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
               case "main":
               case "change":
               case "":
                    $this->callView("knjz422Form1");
                    break 2;
               case "update":
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getInsertModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("");
                    break 1;
               case "error":
                    $this->callView("error");
                    break 2;
               case "reset":
                    $sessionInstance->setCmd("");
                    break 1;
               default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjz422Ctl = new knjz422Controller;
//var_dump($_REQUEST);
?>