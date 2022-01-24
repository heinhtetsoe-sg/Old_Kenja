<?php

require_once('for_php7.php');

require_once('knjd653Model.inc');
require_once('knjd653Query.inc');

class knjd653Controller extends Controller {
    var $ModelClassName = "knjd653Model";
    var $ProgramID      = "KNJD653";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd653":
                case "gakki":
                    $sessionInstance->knjd653Model();
                    $this->callView("knjd653Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd653Ctl = new knjd653Controller;
var_dump($_REQUEST);
?>
