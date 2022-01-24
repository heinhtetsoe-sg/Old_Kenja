<?php

require_once('for_php7.php');

require_once('knjl071rModel.inc');
require_once('knjl071rQuery.inc');

class knjl071rController extends Controller {
    var $ModelClassName = "knjl071rModel";
    var $ProgramID      = "KNJL071R";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "changeApp":
                case "changeTest":
                case "changeSh":
                case "main":
                case "read":
                case "reset":
                    $this->callView("knjl071rForm1");
                    break 2;
                case "update":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("read");
                    break 1;
                case "":
                    $this->callView("knjl071rForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl071rCtl = new knjl071rController;
?>
