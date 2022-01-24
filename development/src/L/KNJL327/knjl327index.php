<?php

require_once('for_php7.php');

require_once('knjl327Model.inc');
require_once('knjl327Query.inc');

class knjl327Controller extends Controller {
    var $ModelClassName = "knjl327Model";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl327":
                    $sessionInstance->knjl327Model();
                    $this->callView("knjl327Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
			}
        }
            
    }
}
$knjl327Ctl = new knjl327Controller;
var_dump($_REQUEST);
?>
