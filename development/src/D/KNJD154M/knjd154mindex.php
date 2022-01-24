<?php

require_once('for_php7.php');

require_once('knjd154mModel.inc');
require_once('knjd154mQuery.inc');

class knjd154mController extends Controller {
    var $ModelClassName = "knjd154mModel";
    var $ProgramID      = "KNJD154M";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "clear";
                case "knjd154m";
                    $sessionInstance->knjd154mModel();
                    $this->callView("knjd154mForm1");
                    exit;
                case "main":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $sessionInstance->knjd154mModel();
                    $this->callView("knjd154mForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd154mCtl = new knjd154mController;
?>
