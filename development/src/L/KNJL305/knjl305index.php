<?php

require_once('for_php7.php');

require_once('knjl305Model.inc');
require_once('knjl305Query.inc');

class knjl305Controller extends Controller {
    var $ModelClassName = "knjl305Model";
    var $ProgramID      = "KNJL305";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl305":
                    $sessionInstance->knjl305Model();
                    $this->callView("knjl305Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
			}
        }
            
    }
}
$knjl305Ctl = new knjl305Controller;
//var_dump($_REQUEST);
?>
