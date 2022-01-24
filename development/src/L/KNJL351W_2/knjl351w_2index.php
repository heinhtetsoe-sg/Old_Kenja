<?php

require_once('for_php7.php');

require_once('knjl351w_2Model.inc');
require_once('knjl351w_2Query.inc');

class knjl351w_2Controller extends Controller {
    var $ModelClassName = "knjl351w_2Model";
    var $ProgramID      = "KNJL351W_2";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "cancel":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $sessionInstance->getCancelModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "":
                case "main":
                    $sessionInstance->getMainModel();
                    $this->callView("knjl351w_2Form1");
                    break 2;
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
$knjl351w_2Ctl = new knjl351w_2Controller;
?>
