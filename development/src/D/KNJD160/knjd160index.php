<?php

require_once('for_php7.php');

require_once('knjd160Model.inc');
require_once('knjd160Query.inc');

class knjd160Controller extends Controller {
    var $ModelClassName = "knjd160Model";
    var $ProgramID      = "KNJD160";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd160":
                    $sessionInstance->knjd160Model();
                    $this->callView("knjd160Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
			}
        }
            
    }
}
$knjd160Ctl = new knjd160Controller;
var_dump($_REQUEST);
?>
