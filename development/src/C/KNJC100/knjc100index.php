<?php

require_once('for_php7.php');

require_once('knjc100Model.inc');
require_once('knjc100Query.inc');

class knjc100Controller extends Controller {
    var $ModelClassName = "knjc100Model";
    var $ProgramID      = "KNJC100";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjc100":
                case "semechg":
                    $sessionInstance->knjc100Model();
                    $this->callView("knjc100Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knjc100Ctl = new knjc100Controller;
//var_dump($_REQUEST);
?>
