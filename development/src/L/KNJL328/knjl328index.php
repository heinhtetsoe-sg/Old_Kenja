<?php

require_once('for_php7.php');

require_once('knjl328Model.inc');
require_once('knjl328Query.inc');

class knjl328Controller extends Controller {
    var $ModelClassName = "knjl328Model";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl328":
                    $sessionInstance->knjl328Model();
                    $this->callView("knjl328Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
			}
        }
            
    }
}
$knjl328Ctl = new knjl328Controller;
var_dump($_REQUEST);
?>
