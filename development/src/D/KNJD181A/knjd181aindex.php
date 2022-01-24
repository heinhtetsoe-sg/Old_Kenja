<?php

require_once('for_php7.php');

require_once('knjd181aModel.inc');
require_once('knjd181aQuery.inc');

class knjd181aController extends Controller {
    var $ModelClassName = "knjd181aModel";
    var $ProgramID      = "KNJD181A";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "main":
                case "clear";
                case "knjd181a";
                    $sessionInstance->knjd181aModel();
                    $this->callView("knjd181aForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd181aCtl = new knjd181aController;
?>
