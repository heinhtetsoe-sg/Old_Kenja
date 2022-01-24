<?php

require_once('for_php7.php');

require_once('knjb3022Model.inc');
require_once('knjb3022Query.inc');

class knjb3022Controller extends Controller {
    var $ModelClassName = "knjb3022Model";
    var $ProgramID      = "KNJB3022";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "edit":
                case "editStaff":
                case "reset":
                case "getHrClass":
                case "getClass":
                case "getSubclass":
                case "getCreditHrClass":
                    $this->callView("knjb3022Form1");
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
$knjb3022Ctl = new knjb3022Controller;
//var_dump($_REQUEST);
?>
