<?php

require_once('for_php7.php');

require_once('knjb131Model.inc');
require_once('knjb131Query.inc');

class knjb131Controller extends Controller {
    var $ModelClassName = "knjb131Model";
    var $ProgramID      = "KNJB131";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjb131":
                case "gakki":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $sessionInstance->knjb131Model();
                    $this->callView("knjb131Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjb131Ctl = new knjb131Controller;
//var_dump($_REQUEST);
?>
