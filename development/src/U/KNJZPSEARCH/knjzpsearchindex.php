<?php

require_once('for_php7.php');

require_once('knjzpsearchModel.inc');
require_once('knjzpsearchQuery.inc');
require_once('graph.php');

class knjzpsearchController extends Controller {
    var $ModelClassName = "knjzpsearchModel";
    var $ProgramID      = "KNJZPSEARCH";
    
    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "list":
                case "top_change":
                    $this->callView("knjzpsearchForm1");
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
$knjzpsearchCtl = new knjzpsearchController;
//var_dump($_REQUEST);
?>
