<?php

require_once('for_php7.php');

require_once('knjd426mModel.inc');
require_once('knjd426mQuery.inc');

class knjd426mController extends Controller {
    var $ModelClassName = "knjd426mModel";
    var $ProgramID      = "KNJD426M";

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
                case "knjd426m";
                    $sessionInstance->knjd426mModel();
                    $this->callView("knjd426mForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd426mCtl = new knjd426mController;
?>
