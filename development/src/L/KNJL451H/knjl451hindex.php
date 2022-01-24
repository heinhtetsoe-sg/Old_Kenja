<?php

require_once('for_php7.php');

require_once('knjl451hModel.inc');
require_once('knjl451hQuery.inc');

class knjl451hController extends Controller
{
    public $ModelClassName = "knjl451hModel";
    public $ProgramID      = "KNJL451H";     //プログラムID

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "edit":
                case "simShow":
                case "decisionShow":
                    $sessionInstance->getMainModel();
                    $this->callView("knjl451hForm1");
                    break 2;
                case "sim":
                    $sessionInstance->getSimModel();
                    $sessionInstance->setCmd("simShow");
                    break 1;
                case "decision":
                    $sessionInstance->getDecisionModel();
                    $sessionInstance->setCmd("decisionShow");
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
$knjl451hCtl = new knjl451hController();
