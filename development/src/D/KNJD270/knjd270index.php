<?php

require_once('for_php7.php');

require_once('knjd270Model.inc');
require_once('knjd270Query.inc');

class knjd270Controller extends Controller {
    var $ModelClassName = "knjd270Model";
    var $ProgramID      = "KNJD270";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "toukei":
                case "":
                    $this->callView("knjd270Form1");
                   break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
            
        }
    }
}
$knjd270Ctl = new knjd270Controller;
?>
