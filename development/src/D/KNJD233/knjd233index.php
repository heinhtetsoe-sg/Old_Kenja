<?php

require_once('for_php7.php');

require_once('knjd233Model.inc');
require_once('knjd233Query.inc');

class knjd233Controller extends Controller {
    var $ModelClassName = "knjd233Model";
    var $ProgramID      = "KNJD233";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd233":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $sessionInstance->knjd233Model();
                    $this->callView("knjd233Form1");
                    exit;
                case "gakki":
                    $sessionInstance->knjd233Model();
                    $this->callView("knjd233Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd233Ctl = new knjd233Controller;
var_dump($_REQUEST);
?>
