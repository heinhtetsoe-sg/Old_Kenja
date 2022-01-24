<?php

require_once('for_php7.php');

require_once('knjp1218Model.inc');
require_once('knjp1218Query.inc');

class knjp1218Controller extends Controller {
    var $ModelClassName = "knjp1218Model";
    var $ProgramID      = "KNJP1218";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "change_class":
                case "knjp1218":
                case "read":
                    $sessionInstance->knjp1218Model();
                    $this->callView("knjp1218Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjp1218Ctl = new knjp1218Controller;
var_dump($_REQUEST);
?>
