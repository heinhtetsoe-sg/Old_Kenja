<?php

require_once('for_php7.php');

require_once('knjl300Model.inc');
require_once('knjl300Query.inc');

class knjl300Controller extends Controller {
    var $ModelClassName = "knjl300Model";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl300":
                    $sessionInstance->knjl300Model();
                    $this->callView("knjl300Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
			}
        }
            
    }
}
$knjl300Ctl = new knjl300Controller;
var_dump($_REQUEST);
?>
