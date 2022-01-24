<?php

require_once('for_php7.php');

require_once('knjl314Model.inc');
require_once('knjl314Query.inc');

class knjl314Controller extends Controller {
    var $ModelClassName = "knjl314Model";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl314":
                    $sessionInstance->knjl314Model();
                    $this->callView("knjl314Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
			}
        }
            
    }
}
$knjl314Ctl = new knjl314Controller;
var_dump($_REQUEST);
?>
