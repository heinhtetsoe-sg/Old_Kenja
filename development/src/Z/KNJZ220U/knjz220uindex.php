<?php

require_once('for_php7.php');

require_once('knjz220uModel.inc');
require_once('knjz220uQuery.inc');

class knjz220uController extends Controller {
    var $ModelClassName = "knjz220uModel";
    var $ProgramID          = "KNJZ220U";

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
               case "error":
                    $this->callView("error");
                    break 2;
               case "level":
               case "clear":
               case "main":
               case "":
                    $this->callView("knjz220uForm1");
                    break 2;
               default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
            
        }
    }
}
$knjz220uCtl = new knjz220uController;
//var_dump($_REQUEST);
?>
