<?php

require_once('for_php7.php');

require_once('knjj193Model.inc');
require_once('knjj193Query.inc');

class knjj193Controller extends Controller {
    var $ModelClassName = "knjj193Model";
    var $ProgramID      = "KNJJ193";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjj193":
                    $sessionInstance->knjj193Model();
                    $this->callView("knjj193Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjj193Ctl = new knjj193Controller;
//var_dump($_REQUEST);
?>
