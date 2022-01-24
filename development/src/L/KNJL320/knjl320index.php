<?php

require_once('for_php7.php');

require_once('knjl320Model.inc');
require_once('knjl320Query.inc');

class knjl320Controller extends Controller {
    var $ModelClassName = "knjl320Model";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl320":
                    $sessionInstance->knjl320Model();
                    $this->callView("knjl320Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
			}
        }
            
    }
}
$knjl320Ctl = new knjl320Controller;
var_dump($_REQUEST);
?>
