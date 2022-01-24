<?php

require_once('for_php7.php');

require_once('knjxclub_committeeModel.inc');
require_once('knjxclub_committeeQuery.inc');

class knjxclub_committeeController extends Controller {
    var $ModelClassName = "knjxclub_committeeModel";
    var $ProgramID      = "knjxclub_committee";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                    $this->callView("knjxclub_committeeForm1");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjxclub_committeeCtl = new knjxclub_committeeController;
?>
