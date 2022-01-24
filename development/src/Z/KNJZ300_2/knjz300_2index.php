<?php

require_once('for_php7.php');

require_once('knjz300_2Model.inc');
require_once('knjz300_2Query.inc');

class knjz300_2Controller extends Controller {
    var $ModelClassName = "knjz300_2Model";
    var $ProgramID      = "KNJZ300";
    
    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "update":
                    $sessionInstance->setAccessLogDetail("U", "KNJZ300_2");
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("sel");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                case "change_kind":
                case "sel";
                case "clear";
                    $sessionInstance->setAccessLogDetail("S", "KNJZ300_2");
                    $this->callView("knjz300_2Form1");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
            
        }
    }
}
$knjz300_2Ctl = new knjz300_2Controller;
//var_dump($_REQUEST);
?>
