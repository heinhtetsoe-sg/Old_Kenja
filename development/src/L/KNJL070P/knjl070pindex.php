<?php

require_once('for_php7.php');

require_once('knjl070pModel.inc');
require_once('knjl070pQuery.inc');

class knjl070pController extends Controller {
    var $ModelClassName = "knjl070pModel";
    var $ProgramID      = "KNJL070P";     //プログラムID

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "edit":
                case "simShow":
                    $sessionInstance->getMainModel();
                    $this->callView("knjl070pForm1");
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
$knjl070pCtl = new knjl070pController;
?>
