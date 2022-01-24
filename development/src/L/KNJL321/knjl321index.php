<?php

require_once('for_php7.php');

require_once('knjl321Model.inc');
require_once('knjl321Query.inc');

class knjl321Controller extends Controller {
    var $ModelClassName = "knjl321Model";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl321":
                    $sessionInstance->knjl321Model();
                    $this->callView("knjl321Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
			}
        }
            
    }
}
$knjl321Ctl = new knjl321Controller;
var_dump($_REQUEST);
?>
