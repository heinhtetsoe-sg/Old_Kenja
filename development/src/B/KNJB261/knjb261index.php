<?php

require_once('for_php7.php');

require_once('knjb261Model.inc');
require_once('knjb261Query.inc');

class knjb261Controller extends Controller {
    var $ModelClassName = "knjb261Model";
    var $ProgramID      = "KNJB261";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjb261":
                case "gakki":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $sessionInstance->knjb261Model();
                    $this->callView("knjb261Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjb261Ctl = new knjb261Controller;
//var_dump($_REQUEST);
?>
