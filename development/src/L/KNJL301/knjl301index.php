<?php

require_once('for_php7.php');

require_once('knjl301Model.inc');
require_once('knjl301Query.inc');

class knjl301Controller extends Controller {
    var $ModelClassName = "knjl301Model";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl301":
                    $sessionInstance->knjl301Model();
                    $this->callView("knjl301Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
			}
        }
            
    }
}
$knjl301Ctl = new knjl301Controller;
var_dump($_REQUEST);
?>
