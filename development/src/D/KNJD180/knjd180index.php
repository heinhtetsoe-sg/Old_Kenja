<?php

require_once('for_php7.php');

require_once('knjd180Model.inc');
require_once('knjd180Query.inc');

class knjd180Controller extends Controller {
    var $ModelClassName = "knjd180Model";
    var $ProgramID      = "KNJD180";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "toukei":
                case "":
                    $this->callView("knjd180Form1");
                   break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
            
        }
    }
}
$knjd180Ctl = new knjd180Controller;
//var_dump($_REQUEST);
?>
