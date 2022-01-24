<?php

require_once('for_php7.php');

require_once('knjl342Model.inc');
require_once('knjl342Query.inc');

class knjl342Controller extends Controller {
    var $ModelClassName = "knjl342Model";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl342":
                    $sessionInstance->knjl342Model();
                    $this->callView("knjl342Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
			}
        }
            
    }
}
$knjl342Ctl = new knjl342Controller;
var_dump($_REQUEST);
?>
