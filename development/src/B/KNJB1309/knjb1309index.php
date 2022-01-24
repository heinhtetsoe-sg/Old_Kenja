<?php

require_once('for_php7.php');

require_once('knjb1309Model.inc');
require_once('knjb1309Query.inc');

class knjb1309Controller extends Controller {
    var $ModelClassName = "knjb1309Model";
    var $ProgramID      = "KNJB1309";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjb1309":
                    $sessionInstance->knjb1309Model();
                    $this->callView("knjb1309Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjb1309Ctl = new knjb1309Controller;
//var_dump($_REQUEST);
?>
