<?php

require_once('for_php7.php');

require_once('knjd429mModel.inc');
require_once('knjd429mQuery.inc');

class knjd429mController extends Controller {
    var $ModelClassName = "knjd429mModel";
    var $ProgramID      = "KNJD429M";

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
                case "knjd429m";
                    $sessionInstance->knjd429mModel();
                    $this->callView("knjd429mForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd429mCtl = new knjd429mController;
?>
