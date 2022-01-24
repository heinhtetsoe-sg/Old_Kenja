<?php

require_once('for_php7.php');

require_once('knjl072aModel.inc');
require_once('knjl072aQuery.inc');

class knjl072aController extends Controller
{
    public $ModelClassName = "knjl072aModel";
    public $ProgramID      = "KNJL072A";     //プログラムID

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
                    $this->callView("knjl072aForm1");
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
$knjl072aCtl = new knjl072aController();
