<?php

require_once('for_php7.php');

require_once('knjd219hModel.inc');
require_once('knjd219hQuery.inc');

class knjd219hController extends Controller {
    var $ModelClassName = "knjd219hModel";
    var $ProgramID      = "KNJD219H";

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
                    $this->callView("knjd219hForm1");
                    break 2;
               default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd219hCtl = new knjd219hController;
//var_dump($_REQUEST);
?>
