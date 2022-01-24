<?php

require_once('for_php7.php');

require_once('knjd219jModel.inc');
require_once('knjd219jQuery.inc');

class knjd219jController extends Controller {
    var $ModelClassName = "knjd219jModel";
    var $ProgramID      = "KNJD219J";

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
                case "delete":
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getDeleteModel();
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
                    $this->callView("knjd219jForm1");
                    break 2;
               default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd219jCtl = new knjd219jController;
//var_dump($_REQUEST);
?>
