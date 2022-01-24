<?php

require_once('for_php7.php');

require_once('knjs551Model.inc');
require_once('knjs551Query.inc');

class knjs551Controller extends Controller {
    var $ModelClassName = "knjs551Model";
    var $ProgramID          = "KNJS551";

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
                    $this->callView("knjs551Form1");
                    break 2;
               case "main":
                    $this->callView("knjs551Form1");
                    break 2;
               case "shokiti":
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getShokitiModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("");
                    break 1;
               case "year_add";
                    $this->callView("knjs551Form1");
                    break 2;
               default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjs551Ctl = new knjs551Controller;
//var_dump($_REQUEST);
?>