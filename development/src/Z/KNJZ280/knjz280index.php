<?php

require_once('for_php7.php');

require_once('knjz280Model.inc');
require_once('knjz280Query.inc');

class KNJZ280Controller extends Controller {
    var $ModelClassName = "KNJZ280Model";
    var $ProgramID      = "KNJZ280";
	
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
                    $this->callView("sel");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
            
        }
    }
}
$KNJZ280Ctl = new KNJZ280Controller;
//var_dump($_REQUEST);
?>