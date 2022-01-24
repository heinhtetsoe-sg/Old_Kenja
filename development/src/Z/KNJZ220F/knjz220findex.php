<?php

require_once('for_php7.php');

require_once('knjz220fModel.inc');
require_once('knjz220fQuery.inc');

class knjz220fController extends Controller {
    var $ModelClassName = "knjz220fModel";
    var $ProgramID          = "KNJZ220f";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
               case "update":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getUpdateModel();
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
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjz220fForm1");
                    break 2;
               case "clear":
               case "change":
               case "change_year":
               case "main":
               case "default":
               case "":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjz220fForm1");
                    break 2;
               case "copy":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getCopyModel();
                    $sessionInstance->setCmd("main");
                    break 1;
               default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
            
        }
    }
}
$knjz220fCtl = new knjz220fController;
//var_dump($_REQUEST);
?>
