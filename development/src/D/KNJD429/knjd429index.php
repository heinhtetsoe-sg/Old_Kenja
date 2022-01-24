<?php

require_once('for_php7.php');

require_once('knjd429Model.inc');
require_once('knjd429Query.inc');

class knjd429Controller extends Controller {
    var $ModelClassName = "knjd429Model";
    var $ProgramID      = "KNJD429";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "change":
                case "changeHukusiki":
                case "main":
                case "seldate":
                case "clear";
                case "knjd429";
                    $sessionInstance->knjd429Model();
                    $this->callView("knjd429Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd429Ctl = new knjd429Controller;
?>
