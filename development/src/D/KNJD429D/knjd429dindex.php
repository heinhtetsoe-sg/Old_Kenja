<?php

require_once('for_php7.php');

require_once('knjd429dModel.inc');
require_once('knjd429dQuery.inc');

class knjd429dController extends Controller {
    var $ModelClassName = "knjd429dModel";
    var $ProgramID      = "KNJD429D";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "change":
                case "changeHukusiki":
                case "changeSchoolKind":
                case "changeGhr":
                case "main":
                case "seldate":
                case "clear";
                case "knjd429d";
                    $sessionInstance->knjd429dModel();
                    $this->callView("knjd429dForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd429dCtl = new knjd429dController;
?>
