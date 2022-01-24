<?php

require_once('for_php7.php');

require_once('knjh45psearchModel.inc');
require_once('knjh45psearchQuery.inc');
require_once('graph.php');

class knjh45psearchController extends Controller {
    var $ModelClassName = "knjh45psearchModel";
    var $ProgramID      = "KNJH45PSEARCH";
    
    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "list":
                case "top_change":
                    $this->callView("knjh45psearchForm1");
                    break 2;
                case "delete":
                    $sessionInstance->getDeleteModel();
                    $sessionInstance->setCmd("comp");
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
$knjh45psearchCtl = new knjh45psearchController;
//var_dump($_REQUEST);
?>
