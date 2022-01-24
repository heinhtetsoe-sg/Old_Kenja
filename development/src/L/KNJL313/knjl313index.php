<?php

require_once('for_php7.php');

require_once('knjl313Model.inc');
require_once('knjl313Query.inc');

class knjl313Controller extends Controller {
    var $ModelClassName = "knjl313Model";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl313":
                    $sessionInstance->knjl313Model();
                    $this->callView("knjl313Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
			}
        }
            
    }
}
$knjl313Ctl = new knjl313Controller;
var_dump($_REQUEST);
?>
