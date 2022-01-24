<?php
require_once('knjvexpModel.inc');
require_once('knjvexpQuery.inc');

class knjvexpController extends Controller {
    var $ModelClassName = "knjvexpModel";
    var $ProgramID      = "KNJvexp";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "right":
                    $this->callView("knjxSearch");
                    break 2;
                case "list":
                case "search":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $this->callView("knjvexpForm1");
                    break 2;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    $sessionInstance->setCmd("list");
                    break 1;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjvexpCtl = new knjvexpController;
//var_dump($_REQUEST);
?>
