<?php

require_once('for_php7.php');

require_once('knjd179Model.inc');
require_once('knjd179Query.inc');

class knjd179Controller extends Controller {
    var $ModelClassName = "knjd179Model";
    var $ProgramID      = "KNJD179";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "main":
                case "clear";
                case "knjd179";
                    $sessionInstance->knjd179Model();
                    $this->callView("knjd179Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd179Ctl = new knjd179Controller;
?>
