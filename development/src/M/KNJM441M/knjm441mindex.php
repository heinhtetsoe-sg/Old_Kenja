<?php

require_once('for_php7.php');

require_once('knjm441mModel.inc');
require_once('knjm441mQuery.inc');

class knjm441mController extends Controller {
    var $ModelClassName = "knjm441mModel";
    var $ProgramID      = "KNJM441M";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "main":
                case "knjm441m":
                    $sessionInstance->knjm441mModel();
                    $this->callView("knjm441mForm1");
                    exit;
                case "update":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjm441mCtl = new knjm441mController;
?>
