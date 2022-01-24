<?php

require_once('for_php7.php');

require_once('knjd060Model.inc');
require_once('knjd060Query.inc');

class knjd060Controller extends Controller {
    var $ModelClassName = "knjd060Model";
    var $ProgramID      = "KNJD060";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd060":
                case "semechg":
                    $sessionInstance->knjd060Model();
                    $this->callView("knjd060Form1");
                    exit;
                case "gakki":
                    $sessionInstance->knjd060Model();
                    $this->callView("knjd060Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
			}
        }
            
    }
}
$knjd060Ctl = new knjd060Controller;
var_dump($_REQUEST);
?>
