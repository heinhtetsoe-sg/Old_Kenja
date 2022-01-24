<?php

require_once('for_php7.php');

require_once('knjd625Model.inc');
require_once('knjd625Query.inc');

class knjd625Controller extends Controller {
    var $ModelClassName = "knjd625Model";
    var $ProgramID      = "KNJD625";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd625":
                    $sessionInstance->knjd625Model();
                    $this->callView("knjd625Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }

    }
}
$knjd625Ctl = new knjd625Controller;
var_dump($_REQUEST);
?>
