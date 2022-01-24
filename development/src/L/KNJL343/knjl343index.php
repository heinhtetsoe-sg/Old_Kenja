<?php

require_once('for_php7.php');

require_once('knjl343Model.inc');
require_once('knjl343Query.inc');

class knjl343Controller extends Controller {
    var $ModelClassName = "knjl343Model";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl343":
                case "print":
                    $sessionInstance->knjl343Model();
                    $this->callView("knjl343Form1");
                    exit;
                case "printCheck":
                    $sessionInstance->getPrintCheck();
                    $sessionInstance->setCmd("print");
                    break 1;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
			}
        }
            
    }
}
$knjl343Ctl = new knjl343Controller;
var_dump($_REQUEST);
?>
