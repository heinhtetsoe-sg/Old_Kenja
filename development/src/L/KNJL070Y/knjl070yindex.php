<?php

require_once('for_php7.php');

require_once('knjl070yModel.inc');
require_once('knjl070yQuery.inc');

class knjl070yController extends Controller {
    var $ModelClassName = "knjl070yModel";
    var $ProgramID      = "KNJL070Y";     //プログラムID

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "edit":
                    $sessionInstance->getMainModel();
                    $this->callView("knjl070yForm1");
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
$knjl070yCtl = new knjl070yController;
?>
