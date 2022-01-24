<?php

require_once('for_php7.php');

require_once('knjf100eModel.inc');
require_once('knjf100eQuery.inc');

class knjf100eController extends Controller {
    var $ModelClassName = "knjf100eModel";
    var $ProgramID      = "KNJF100E";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjf100e":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $sessionInstance->knjf100eModel();
                    $this->callView("knjf100eForm1");
                    exit;
                case "error":
                    $this->callView("error");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjf100eCtl = new knjf100eController;
?>
