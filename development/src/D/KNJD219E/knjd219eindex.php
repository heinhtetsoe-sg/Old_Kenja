<?php

require_once('for_php7.php');

require_once('knjd219eModel.inc');
require_once('knjd219eQuery.inc');

class knjd219eController extends Controller {
    var $ModelClassName = "knjd219eModel";
    var $ProgramID      = "KNJD219E";

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
               case "copy":
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getCopyModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("main");
                    break 1;
               case "error":
                    $this->callView("error");
                    break 2;
               case "sim":
                    $sessionInstance->getSimModel();
                    $this->callView("knjd219eForm1");
                    break 2;
               case "inquiry":
                    $this->callView("knjd219eForm2");
                    break 2;
               case "clear":
               case "change":
               case "main":
               case "default":
               case "":
                    $this->callView("knjd219eForm1");
                    break 2;
               default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd219eCtl = new knjd219eController;
//var_dump($_REQUEST);
?>
