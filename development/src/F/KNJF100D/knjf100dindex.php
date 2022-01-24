<?php

require_once('for_php7.php');

require_once('knjf100dModel.inc');
require_once('knjf100dQuery.inc');

class knjf100dController extends Controller {
    var $ModelClassName = "knjf100dModel";
    var $ProgramID      = "KNJF100D";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjf100d":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $sessionInstance->knjf100dModel();
                    $this->callView("knjf100dForm1");
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
$knjf100dCtl = new knjf100dController;
?>
