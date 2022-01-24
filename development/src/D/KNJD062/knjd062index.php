<?php

require_once('for_php7.php');

require_once('knjd062Model.inc');
require_once('knjd062Query.inc');

class knjd062Controller extends Controller {
    var $ModelClassName = "knjd062Model";
    var $ProgramID      = "KNJD062";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd062":
                case "gakki":
                    $sessionInstance->knjd062Model();
                    $this->callView("knjd062Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
			}
        }
            
    }
}
$knjd062Ctl = new knjd062Controller;
var_dump($_REQUEST);
?>
