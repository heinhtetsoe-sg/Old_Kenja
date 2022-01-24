<?php

require_once('for_php7.php');

require_once('knjl347Model.inc');
require_once('knjl347Query.inc');

class knjl347Controller extends Controller {
    var $ModelClassName = "knjl347Model";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl347":
                    $sessionInstance->knjl347Model();
                    $this->callView("knjl347Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
			}
        }
            
    }
}
$knjl347Ctl = new knjl347Controller;
var_dump($_REQUEST);
?>
