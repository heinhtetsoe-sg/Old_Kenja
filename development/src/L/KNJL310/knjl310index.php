<?php

require_once('for_php7.php');

require_once('knjl310Model.inc');
require_once('knjl310Query.inc');

class knjl310Controller extends Controller {
    var $ModelClassName = "knjl310Model";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl310":
                    $sessionInstance->knjl310Model();
                    $this->callView("knjl310Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
			}
        }
            
    }
}
$knjl310Ctl = new knjl310Controller;
var_dump($_REQUEST);
?>
