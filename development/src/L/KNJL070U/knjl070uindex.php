<?php

require_once('for_php7.php');

require_once('knjl070uModel.inc');
require_once('knjl070uQuery.inc');

class knjl070uController extends Controller {
    var $ModelClassName = "knjl070uModel";
    var $ProgramID      = "KNJL070U";     //プログラムID

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "edit":
                case "simShow":
                    $sessionInstance->getMainModel();
                    $this->callView("knjl070uForm1");
                    break 2;
                case "sim":
                    $sessionInstance->getSimModel();
                    $sessionInstance->setCmd("simShow");
                    break 1;
                case "decision":
                    $sessionInstance->getDecisionModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "":
                    $sessionInstance->getJudgeTmpClearModel();
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
$knjl070uCtl = new knjl070uController;
?>
