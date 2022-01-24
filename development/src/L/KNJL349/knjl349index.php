<?php

require_once('for_php7.php');

require_once('knjl349Model.inc');
require_once('knjl349Query.inc');

class knjl349Controller extends Controller {
    var $ModelClassName = "knjl349Model";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl349":
                    $sessionInstance->knjl349Model();
                    $this->callView("knjl349Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
			}
        }
            
    }
}
$knjl349Ctl = new knjl349Controller;
var_dump($_REQUEST);
?>
