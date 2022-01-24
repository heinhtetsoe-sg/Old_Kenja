<?php

require_once('for_php7.php');

require_once('knjs530Model.inc');
require_once('knjs530Query.inc');

class knjs530Controller extends Controller {
    var $ModelClassName = "knjs530Model";
    var $ProgramID          = "KNJS530";

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
                    $this->callView("knjs530Form1");
                    break 2;
               case "main":
                    $this->callView("knjs530Form1");
                    break 2;
               case "monthmain":
                    $this->callView("knjs530Form1");
                    break 2;
               case "shokitiyear":
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getInsert2Model();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("");
                    break 1;
               case "shokitimonth":
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getInsert2Model();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("");
                    break 1;
               case "copy_year":
               case "copy_month":
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getCopyModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("");
                    break 1;
               case "year_add";
                    $this->callView("knjs530Form1");
                    break 2;
               default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjs530Ctl = new knjs530Controller;
//var_dump($_REQUEST);
?>