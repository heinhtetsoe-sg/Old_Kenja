<?php

require_once('for_php7.php');

require_once('knjh45msearchModel.inc');
require_once('knjh45msearchQuery.inc');
require_once('graph.php');

class knjh45msearchController extends Controller {
    var $ModelClassName = "knjh45msearchModel";
    var $ProgramID      = "KNJH45MSEARCH";
    
    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "list":
                case "top_change":
                    $this->callView("knjh45msearchForm1");
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
$knjh45msearchCtl = new knjh45msearchController;
//var_dump($_REQUEST);
?>
