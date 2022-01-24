<?php

require_once('for_php7.php');

require_once('knjd426Model.inc');
require_once('knjd426Query.inc');

class knjd426Controller extends Controller {
    var $ModelClassName = "knjd426Model";
    var $ProgramID      = "KNJD426";

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
                case "knjd426";
                    $sessionInstance->knjd426Model();
                    $this->callView("knjd426Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd426Ctl = new knjd426Controller;
?>
