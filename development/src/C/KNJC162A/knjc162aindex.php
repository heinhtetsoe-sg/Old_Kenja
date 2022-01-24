<?php

require_once('for_php7.php');

require_once('knjc162aModel.inc');
require_once('knjc162aQuery.inc');

class knjc162aController extends Controller {
    var $ModelClassName = "knjc162aModel";
    var $ProgramID      = "KNJC162A";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjc162a":
                    $sessionInstance->knjc162aModel();
                    $this->callView("knjc162aForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjc162aCtl = new knjc162aController;
?>
