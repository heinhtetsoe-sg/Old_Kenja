<?php

require_once('for_php7.php');

require_once('knjs550Model.inc');
require_once('knjs550Query.inc');

class knjs550Controller extends Controller {
    var $ModelClassName = "knjs550Model";
    var $ProgramID      = "KNJS550";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
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
               case "":
                    $this->callView("knjs550Form1");
                    break 2;
               case "copy":
                    $sessionInstance->getCopyModel();
                    $sessionInstance->setCmd("main");
                    break 1;
               case "main":
                    $this->callView("knjs550Form1");
                    break 2;
               default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjs550Ctl = new knjs550Controller;
//var_dump($_REQUEST);
?>