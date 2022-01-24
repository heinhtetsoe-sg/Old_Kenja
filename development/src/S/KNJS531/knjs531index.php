<?php

require_once('for_php7.php');

require_once('knjs531Model.inc');
require_once('knjs531Query.inc');

class knjs531Controller extends Controller {
    var $ModelClassName = "knjs531Model";
    var $ProgramID          = "KNJS531";

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
                    $this->callView("knjs531Form1");
                    break 2;
               case "main":
               case "batch":
               case "back":
                    $this->callView("knjs531Form1");
                    break 2;
               case "shokitiyear":
               case "shokitimonth":
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getInsert2Model();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("");
                    break 1;
               case "sch_shokiti":
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getInsert3Model();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("");
                    break 1;
               case "year_add";
                    $this->callView("knjs531Form1");
                    break 2;
               default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjs531Ctl = new knjs531Controller;
//var_dump($_REQUEST);
?>