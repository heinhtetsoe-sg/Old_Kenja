<?php

require_once('for_php7.php');

require_once('knjf100cModel.inc');
require_once('knjf100cQuery.inc');

class knjf100cController extends Controller {
    var $ModelClassName = "knjf100cModel";
    var $ProgramID      = "KNJF100C";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjf100c":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $sessionInstance->knjf100cModel();
                    $this->callView("knjf100cForm1");
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
$knjf100cCtl = new knjf100cController;
?>
