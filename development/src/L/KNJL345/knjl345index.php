<?php

require_once('for_php7.php');

require_once('knjl345Model.inc');
require_once('knjl345Query.inc');

class knjl345Controller extends Controller {
    var $ModelClassName = "knjl345Model";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl345":
                    $sessionInstance->knjl345Model();
                    $this->callView("knjl345Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
			}
        }
            
    }
}
$knjl345Ctl = new knjl345Controller;
var_dump($_REQUEST);
?>
