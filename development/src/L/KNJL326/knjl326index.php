<?php

require_once('for_php7.php');

require_once('knjl326Model.inc');
require_once('knjl326Query.inc');

class knjl326Controller extends Controller {
    var $ModelClassName = "knjl326Model";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl326":
                    $sessionInstance->knjl326Model();
                    $this->callView("knjl326Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
			}
        }
            
    }
}
$knjl326Ctl = new knjl326Controller;
var_dump($_REQUEST);
?>
