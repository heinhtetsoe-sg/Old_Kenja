<?php

require_once('for_php7.php');

require_once('knjl070jModel.inc');
require_once('knjl070jQuery.inc');

class knjl070jController extends Controller {
    var $ModelClassName = "knjl070jModel";
    var $ProgramID      = "KNJL070J";     //プログラムID

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "edit":
                    $sessionInstance->getMainModel();
                    $this->callView("knjl070jForm1");
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
$knjl070jCtl = new knjl070jController;
?>
