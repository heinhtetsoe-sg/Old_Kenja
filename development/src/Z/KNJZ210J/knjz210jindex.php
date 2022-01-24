<?php

require_once('for_php7.php');

require_once('knjz210jModel.inc');
require_once('knjz210jQuery.inc');

class knjz210jController extends Controller {
    var $ModelClassName = "knjz210jModel";
    var $ProgramID          = "KNJZ210J";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
               case "update":
                    $this->checkAuth(DEF_UPDATABLE);
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
               case "delete":
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getDeleteModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("main");
                    break 1;
               case "error":
                    $this->callView("error");
                    break 2;
               case "level":
                    $this->callView("knjz210jForm1");
                    break 2;
               case "clear":
               case "change":
               case "main":
               case "default":
               case "":
                    $this->callView("knjz210jForm1");
                    break 2;
               default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjz210jCtl = new knjz210jController;
//var_dump($_REQUEST);
?>
