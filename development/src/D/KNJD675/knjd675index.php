<?php

require_once('for_php7.php');

require_once('knjd675Model.inc');
require_once('knjd675Query.inc');

class knjd675Controller extends Controller {
    var $ModelClassName = "knjd675Model";
    var $ProgramID      = "KNJD675";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd675":
                    $sessionInstance->knjd675Model();
                    $this->callView("knjd675Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd675Ctl = new knjd675Controller;
?>
