<?php

require_once('for_php7.php');

require_once('knjd128qModel.inc');
require_once('knjd128qQuery.inc');

class knjd128qController extends Controller {
    var $ModelClassName = "knjd128qModel";
    var $ProgramID      = "KNJD128Q";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "edit":
                case "reset":
                    $this->callView("knjd128qForm1");
                    break 2;
                case "grpform":
                    $this->callView("knjd128qgrpForm1");
                    break 2;
                case "update":  //更新
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }            
        }
    }

}
$knjd128qCtl = new knjd128qController;
//var_dump($_REQUEST);
?>
