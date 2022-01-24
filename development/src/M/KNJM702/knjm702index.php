<?php

require_once('for_php7.php');

require_once('knjm702Model.inc');
require_once('knjm702Query.inc');

class knjm702Controller extends Controller {
    var $ModelClassName = "knjm702Model";
    var $ProgramID      = "KNJM702";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "main":
                case "knjm702";
                    $sessionInstance->knjm702Model();
                    $this->callView("knjm702Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjm702Ctl = new knjm702Controller;
?>
