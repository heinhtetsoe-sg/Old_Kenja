<?php

require_once('for_php7.php');

require_once('knjl340Model.inc');
require_once('knjl340Query.inc');

class knjl340Controller extends Controller {
    var $ModelClassName = "knjl340Model";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl340":
                    $sessionInstance->knjl340Model();
                    $this->callView("knjl340Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
			}
        }
            
    }
}
$knjl340Ctl = new knjl340Controller;
var_dump($_REQUEST);
?>
