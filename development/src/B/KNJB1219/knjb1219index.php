<?php

require_once('for_php7.php');

require_once('knjb1219Model.inc');
require_once('knjb1219Query.inc');

class knjb1219Controller extends Controller {
    var $ModelClassName = "knjb1219Model";
    var $ProgramID      = "KNJB1219";

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
                case "knjb1219";
                    $sessionInstance->knjb1219Model();
                    $this->callView("knjb1219Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjb1219Ctl = new knjb1219Controller;
?>
