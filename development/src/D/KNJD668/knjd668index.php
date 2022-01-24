<?php

require_once('for_php7.php');

require_once('knjd668Model.inc');
require_once('knjd668Query.inc');

class knjd668Controller extends Controller {
    var $ModelClassName = "knjd668Model";
    var $ProgramID      = "KNJD668";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd668":
                    $sessionInstance->knjd668Model();
                    $this->callView("knjd668Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd668Ctl = new knjd668Controller;
?>
