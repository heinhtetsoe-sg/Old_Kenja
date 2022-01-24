<?php

require_once('for_php7.php');

require_once('knjd186hModel.inc');
require_once('knjd186hQuery.inc');

class knjd186hController extends Controller {
    var $ModelClassName = "knjd186hModel";
    var $ProgramID      = "KNJD186H";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "main":
                case "clear";
                case "knjd186h";
                    $sessionInstance->knjd186hModel();
                    $this->callView("knjd186hForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd186hCtl = new knjd186hController;
?>
