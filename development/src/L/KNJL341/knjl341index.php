<?php

require_once('for_php7.php');

require_once('knjl341Model.inc');
require_once('knjl341Query.inc');

class knjl341Controller extends Controller {
    var $ModelClassName = "knjl341Model";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl341":
                    $sessionInstance->knjl341Model();
                    $this->callView("knjl341Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
			}
        }
            
    }
}
$knjl341Ctl = new knjl341Controller;
var_dump($_REQUEST);
?>
