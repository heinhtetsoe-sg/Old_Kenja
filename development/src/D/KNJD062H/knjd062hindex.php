<?php

require_once('for_php7.php');

require_once('knjd062hModel.inc');
require_once('knjd062hQuery.inc');

class knjd062hController extends Controller {
    var $ModelClassName = "knjd062hModel";
    var $ProgramID      = "KNJD062H";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd062h":
                case "semechg":
                    $sessionInstance->knjd062hModel();
                    $this->callView("knjd062hForm1");
                    exit;
                case "gakki":
                    $sessionInstance->knjd062hModel();
                    $this->callView("knjd062hForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
			}
        }
            
    }
}
$knjd062hCtl = new knjd062hController;
var_dump($_REQUEST);
?>
