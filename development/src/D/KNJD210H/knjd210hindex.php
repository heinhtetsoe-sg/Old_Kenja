<?php

require_once('for_php7.php');

require_once('knjd210hModel.inc');
require_once('knjd210hQuery.inc');

class knjd210hController extends Controller {
    var $ModelClassName = "knjd210hModel";
    var $ProgramID          = "KNJD210H";

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
                    $this->callView("knjd210hForm1");
                    break 2;
               default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
            
        }
    }
}
$knjd210hCtl = new knjd210hController;
//var_dump($_REQUEST);
?>
