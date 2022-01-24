<?php

require_once('for_php7.php');

require_once('knjd187aModel.inc');
require_once('knjd187aQuery.inc');

class knjd187aController extends Controller {
    var $ModelClassName = "knjd187aModel";
    var $ProgramID      = "KNJD187A";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd187a":
                    $sessionInstance->knjd187aModel();
                    $this->callView("knjd187aForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd187aCtl = new knjd187aController;
?>
