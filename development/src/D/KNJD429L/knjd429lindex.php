<?php

require_once('for_php7.php');

require_once('knjd429lModel.inc');
require_once('knjd429lQuery.inc');

class knjd429lController extends Controller {
    var $ModelClassName = "knjd429lModel";
    var $ProgramID      = "KNJD429L";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "change":
                case "changeHukusiki":
                case "main":
                case "seldate":
                case "clear";
                case "knjd429l";
                    $sessionInstance->knjd429lModel();
                    $this->callView("knjd429lForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd429lCtl = new knjd429lController;
?>
