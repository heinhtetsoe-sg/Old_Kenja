<?php

require_once('for_php7.php');

require_once('knjd186uModel.inc');
require_once('knjd186uQuery.inc');

class knjd186uController extends Controller {
    var $ModelClassName = "knjd186uModel";
    var $ProgramID      = "KNJD186U";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd186u":
                    $sessionInstance->knjd186uModel();
                    $this->callView("knjd186uForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd186uCtl = new knjd186uController;
?>
