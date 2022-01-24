<?php

require_once('for_php7.php');

require_once('knjj194Model.inc');
require_once('knjj194Query.inc');

class knjj194Controller extends Controller {
    var $ModelClassName = "knjj194Model";
    var $ProgramID      = "KNJJ194";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjj194":
                    $sessionInstance->knjj194Model();
                    $this->callView("knjj194Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjj194Ctl = new knjj194Controller;
?>
