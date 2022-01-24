<?php

require_once('for_php7.php');

require_once('knjj010Model.inc');
require_once('knjj010Query.inc');

class knjj010Controller extends Controller {
    var $ModelClassName = "knjj010Model";
    var $ProgramID      = "KNJJ010";
	
    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "update":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("sel");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                case "sel";
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjj010Form1");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
            
        }
    }
}
$knjj010Ctl = new knjj010Controller;
//var_dump($_REQUEST);
?>
