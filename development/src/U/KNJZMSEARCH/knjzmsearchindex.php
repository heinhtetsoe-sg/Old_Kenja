<?php

require_once('for_php7.php');

require_once('knjzmsearchModel.inc');
require_once('knjzmsearchQuery.inc');
require_once('graph.php');

class knjzmsearchController extends Controller {
    var $ModelClassName = "knjzmsearchModel";
    var $ProgramID      = "KNJzmsearch";
    
    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "list":
                case "top_change":
                    $this->callView("knjzmsearchForm1");
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
$knjzmsearchCtl = new knjzmsearchController;
//var_dump($_REQUEST);
?>
