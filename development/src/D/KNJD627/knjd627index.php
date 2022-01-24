<?php

require_once('for_php7.php');

require_once('knjd627Model.inc');
require_once('knjd627Query.inc');

class knjd627Controller extends Controller {
    var $ModelClassName = "knjd627Model";
    var $ProgramID      = "KNJD627";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd627":
                    $sessionInstance->knjd627Model();
                    $this->callView("knjd627Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }

    }
}
$knjd627Ctl = new knjd627Controller;
var_dump($_REQUEST);
?>
