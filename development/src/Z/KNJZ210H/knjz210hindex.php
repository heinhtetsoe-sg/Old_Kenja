<?php

require_once('for_php7.php');

require_once('knjz210hModel.inc');
require_once('knjz210hQuery.inc');

class knjz210hController extends Controller {
    var $ModelClassName = "knjz210hModel";
    var $ProgramID          = "KNJZ210H";

    function main()
    {
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
               case "clear":
               case "change":
               case "main":
               case "default":
               case "":
                    $this->callView("knjz210hForm1");
                    break 2;
               default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
            
        }
    }
}
$knjz210hCtl = new knjz210hController;
//var_dump($_REQUEST);
?>
