<?php

require_once('for_php7.php');
require_once('knjl304Model.inc');
require_once('knjl304Query.inc');

class knjl304Controller extends Controller {
    var $ModelClassName = "knjl304Model";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl304":
                    $sessionInstance->knjl304Model();
                    $this->callView("knjl304Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knjl304Ctl = new knjl304Controller;
var_dump($_REQUEST);
?>
