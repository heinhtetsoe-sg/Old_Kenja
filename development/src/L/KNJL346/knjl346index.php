<?php

require_once('for_php7.php');

require_once('knjl346Model.inc');
require_once('knjl346Query.inc');

class knjl346Controller extends Controller {
    var $ModelClassName = "knjl346Model";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl346":
                    $sessionInstance->knjl346Model();
                    $this->callView("knjl346Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
			}
        }
            
    }
}
$knjl346Ctl = new knjl346Controller;
var_dump($_REQUEST);
?>
