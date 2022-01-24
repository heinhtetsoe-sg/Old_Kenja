<?php

require_once('for_php7.php');

require_once('knjm442mModel.inc');
require_once('knjm442mQuery.inc');

class knjm442mController extends Controller {
    var $ModelClassName = "knjm442mModel";
    var $ProgramID      = "KNJM442M";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "main":
                case "knjm442m":
                    $sessionInstance->knjm442mModel();
                    $this->callView("knjm442mForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjm442mCtl = new knjm442mController;
?>
