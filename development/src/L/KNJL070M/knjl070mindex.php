<?php

require_once('for_php7.php');

require_once('knjl070mModel.inc');
require_once('knjl070mQuery.inc');

class knjl070mController extends Controller {
    var $ModelClassName = "knjl070mModel";
    var $ProgramID      = "KNJL070M";     //プログラムID

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "edit":
                    $sessionInstance->getMainModel();
                    $this->callView("knjl070mForm1");
                    break 2;
                case "sim":
                    $sessionInstance->getSimModel();
                    $sessionInstance->setCmd("main");
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
$knjl070mCtl = new knjl070mController;
?>
