<?php

require_once('for_php7.php');

require_once('knjl312Model.inc');
require_once('knjl312Query.inc');

class knjl312Controller extends Controller {
    var $ModelClassName = "knjl312Model";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl312":
                    $sessionInstance->knjl312Model();
                    $this->callView("knjl312Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
			}
        }
            
    }
}
$knjl312Ctl = new knjl312Controller;
var_dump($_REQUEST);
?>
