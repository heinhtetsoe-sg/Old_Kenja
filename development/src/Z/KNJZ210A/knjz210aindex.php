<?php

require_once('for_php7.php');

require_once('knjz210aModel.inc');
require_once('knjz210aQuery.inc');

class knjz210aController extends Controller {
    var $ModelClassName = "knjz210aModel";
    var $ProgramID          = "KNJZ210A";

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
               case "copy":
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getCopyModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("main");
                    break 1;
               case "error":
                    $this->callView("error");
                    break 2;
               case "clear":
               case "main":
               case "default":
               case "":
                    $this->callView("knjz210aForm1");
                    break 2;
               default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
            
        }
    }
}
$knjz210aCtl = new knjz210aController;
//var_dump($_REQUEST);
?>
