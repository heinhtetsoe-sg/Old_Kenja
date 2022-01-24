<?php

require_once('for_php7.php');

require_once('knjs520Model.inc');
require_once('knjs520Query.inc');

class knjs520Controller extends Controller {
    var $ModelClassName = "knjs520Model";
    var $ProgramID          = "KNJS520";

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
                    $this->callView("knjs520Form1");
                    break 2;
               case "main":
                    $this->callView("knjs520Form1");
                    break 2;
               case "yotei":
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getInsert2Model();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("main");
                    break 1;
               case "year_add";
                    $this->callView("knjs520Form1");
                    break 2;
               default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjs520Ctl = new knjs520Controller;
//var_dump($_REQUEST);
?>