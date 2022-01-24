<?php

require_once('for_php7.php');

require_once('knjd629Model.inc');
require_once('knjd629Query.inc');

class knjd629Controller extends Controller {
    var $ModelClassName = "knjd629Model";
    var $ProgramID      = "KNJD629";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd629":
                    $sessionInstance->knjd629Model();
                    $this->callView("knjd629Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd629Ctl = new knjd629Controller;
?>
