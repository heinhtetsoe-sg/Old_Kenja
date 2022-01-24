<?php

require_once('for_php7.php');

require_once('knjd421Model.inc');
require_once('knjd421Query.inc');

class knjd421Controller extends Controller {
    var $ModelClassName = "knjd421Model";
    var $ProgramID      = "KNJD421";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "change":
                case "changeHukusiki":
                case "main":
                case "clear";
                case "knjd421";
                    $sessionInstance->knjd421Model();
                    $this->callView("knjd421Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd421Ctl = new knjd421Controller;
?>
