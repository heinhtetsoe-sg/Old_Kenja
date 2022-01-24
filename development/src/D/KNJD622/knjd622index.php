<?php

require_once('for_php7.php');

require_once('knjd622Model.inc');
require_once('knjd622Query.inc');

class knjd622Controller extends Controller {
    var $ModelClassName = "knjd622Model";
    var $ProgramID      = "KNJD622";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd622":
                    $sessionInstance->knjd622Model();
                    $this->callView("knjd622Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }

    }
}
$knjd622Ctl = new knjd622Controller;
var_dump($_REQUEST);
?>
