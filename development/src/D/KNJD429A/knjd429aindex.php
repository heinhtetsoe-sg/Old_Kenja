<?php

require_once('for_php7.php');

require_once('knjd429aModel.inc');
require_once('knjd429aQuery.inc');

class knjd429aController extends Controller {
    var $ModelClassName = "knjd429aModel";
    var $ProgramID      = "KNJD429A";

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
                case "knjd429a";
                    $sessionInstance->knjd429aModel();
                    $this->callView("knjd429aForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd429aCtl = new knjd429aController;
?>
