<?php

require_once('for_php7.php');

require_once('knjh112Model.inc');
require_once('knjh112Query.inc');

class knjh112Controller extends Controller {
    var $ModelClassName = "knjh112Model";
    var $ProgramID      = "KNJH112";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjh112":
                case "semechg":
                    $sessionInstance->knjh112Model();
                    $this->callView("knjh112Form1");
                    exit;
                case "gakki":
                    $sessionInstance->knjh112Model();
                    $this->callView("knjh112Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjh112Ctl = new knjh112Controller;
var_dump($_REQUEST);
?>
