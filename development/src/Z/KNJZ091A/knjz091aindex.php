<?php

require_once('for_php7.php');

require_once('knjz091aModel.inc');
require_once('knjz091aQuery.inc');

class knjz091aController extends Controller {
    var $ModelClassName = "knjz091aModel";
    var $ProgramID      = "KNJZ091A";
    
    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "update":
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("sel");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                case "sel";
                    $this->callView("knjz091aForm1");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
            
        }
    }
}
$knjz091aCtl = new knjz091aController;
//var_dump($_REQUEST);
?>
