<?php

require_once('for_php7.php');

require_once('knjc039bModel.inc');
require_once('knjc039bQuery.inc');

class knjc039bController extends Controller {
    var $ModelClassName = "knjc039bModel";
    var $ProgramID      = "KNJC039B";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
               case "update":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("main");
                    break 1;
               case "error":
                    $this->callView("error");
                    break 2;
               case "read":
               case "clear":
               case "change":
               case "main":
               case "default":
               case "":
                    $this->callView("knjc039bForm1");
                    break 2;
               default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjc039bCtl = new knjc039bController;
//var_dump($_REQUEST);
?>
