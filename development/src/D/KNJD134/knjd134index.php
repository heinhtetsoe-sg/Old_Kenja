<?php

require_once('for_php7.php');

require_once('knjd134Model.inc');
require_once('knjd134Query.inc');

class knjd134Controller extends Controller {
    var $ModelClassName = "knjd134Model";
    var $ProgramID      = "KNJD134";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd134":
                    $sessionInstance->knjd134Model();
                    $this->callView("knjd134Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd134Ctl = new knjd134Controller;
?>
