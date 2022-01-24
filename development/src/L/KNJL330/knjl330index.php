<?php

require_once('for_php7.php');

require_once('knjl330Model.inc');
require_once('knjl330Query.inc');

class knjl330Controller extends Controller {
    var $ModelClassName = "knjl330Model";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl330":
                    $sessionInstance->knjl330Model();
                    $this->callView("knjl330Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
			}
        }
            
    }
}
$knjl330Ctl = new knjl330Controller;
var_dump($_REQUEST);
?>
