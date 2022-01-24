<?php

require_once('for_php7.php');

require_once('knjl303Model.inc');
require_once('knjl303Query.inc');

class knjl303Controller extends Controller {
    var $ModelClassName = "knjl303Model";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl303":
                    $sessionInstance->knjl303Model();
                    $this->callView("knjl303Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
			}
        }
            
    }
}
$knjl303Ctl = new knjl303Controller;
var_dump($_REQUEST);
?>
