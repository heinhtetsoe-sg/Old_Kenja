<?php

require_once('for_php7.php');

require_once('knjd628Model.inc');
require_once('knjd628Query.inc');

class knjd628Controller extends Controller {
    var $ModelClassName = "knjd628Model";
    var $ProgramID      = "KNJD628";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd628":
                    $sessionInstance->knjd628Model();
                    $this->callView("knjd628Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }

    }
}
$knjd628Ctl = new knjd628Controller;
var_dump($_REQUEST);
?>
