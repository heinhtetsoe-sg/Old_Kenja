<?php

require_once('for_php7.php');

require_once('knjl070bModel.inc');
require_once('knjl070bQuery.inc');

class knjl070bController extends Controller {
    var $ModelClassName = "knjl070bModel";
    var $ProgramID      = "KNJL070B";     //プログラムID

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "edit":
                case "simShow":
                    $sessionInstance->getMainModel();
                    $this->callView("knjl070bForm1");
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
$knjl070bCtl = new knjl070bController;
?>
