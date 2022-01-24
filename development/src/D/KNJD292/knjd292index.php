<?php

require_once('for_php7.php');

require_once('knjd292Model.inc');
require_once('knjd292Query.inc');

class knjd292Controller extends Controller {
    var $ModelClassName = "knjd292Model";
    var $ProgramID      = "KNJD292";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd292":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $sessionInstance->knjd292Model();
                    $this->callView("knjd292Form1");
                    exit;
                case "gakki":
                    $sessionInstance->knjd292Model();
                    $this->callView("knjd292Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd292Ctl = new knjd292Controller;
var_dump($_REQUEST);
?>
