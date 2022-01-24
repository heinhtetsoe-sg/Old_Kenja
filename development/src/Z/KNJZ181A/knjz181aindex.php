<?php

require_once('for_php7.php');

require_once('knjz181aModel.inc');
require_once('knjz181aQuery.inc');

class knjz181aController extends Controller {
    var $ModelClassName = "knjz181aModel";
    var $ProgramID      = "KNJZ181A";

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
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getCopyModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("main");
                    break 1;
               case "error":
                    $this->callView("error");
                    break 2;
               case "level":
                    $this->callView("knjz181aForm1");
                    break 2;
               case "inquiry":
                    $this->callView("knjz181aForm2");
                    break 2;
               case "clear":
               case "change":
               case "main":
               case "default":
               case "":
                    $this->callView("knjz181aForm1");
                    break 2;
               default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjz181aCtl = new knjz181aController;
//var_dump($_REQUEST);
?>
