<?php

require_once('for_php7.php');

require_once('knjl329Model.inc');
require_once('knjl329Query.inc');

class knjl329Controller extends Controller {
    var $ModelClassName = "knjl329Model";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl329":
                    $sessionInstance->knjl329Model();
                    $this->callView("knjl329Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
			}
        }
            
    }
}
$knjl329Ctl = new knjl329Controller;
var_dump($_REQUEST);
?>
