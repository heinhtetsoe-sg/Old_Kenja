<?php

require_once('for_php7.php');

require_once('knjd063Model.inc');
require_once('knjd063Query.inc');

class knjd063Controller extends Controller {
    var $ModelClassName = "knjd063Model";
    var $ProgramID      = "KNJD063";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd063":
                case "semechg":
                    $sessionInstance->knjd063Model();
                    $this->callView("knjd063Form1");
                    exit;
                case "gakki":
                    $sessionInstance->knjd063Model();
                    $this->callView("knjd063Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
			}
        }
            
    }
}
$knjd063Ctl = new knjd063Controller;
var_dump($_REQUEST);
?>
