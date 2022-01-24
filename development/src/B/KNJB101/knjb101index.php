<?php

require_once('for_php7.php');

require_once('knjb101Model.inc');
require_once('knjb101Query.inc');

class knjb101Controller extends Controller {
    var $ModelClassName = "knjb101Model";
    var $ProgramID      = "KNJB101";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "main":
                case "clear";
                    $sessionInstance->knjb101Model();
                    $this->callView("knjb101Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjb101Ctl = new knjb101Controller;
?>
