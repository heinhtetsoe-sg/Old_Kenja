<?php

require_once('for_php7.php');

require_once('knjl770hModel.inc');
require_once('knjl770hQuery.inc');

class knjl770hController extends Controller
{
    public $ModelClassName = "knjl770hModel";
    public $ProgramID      = "KNJL770H";     //プログラムID

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "edit":
                case "simShow":
                    $sessionInstance->getMainModel();
                    $this->callView("knjl770hForm1");
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
                case "clear":
                case "knjl770h":
                    $sessionInstance->getJudgeTmpClearModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "delete":
                    $sessionInstance->getDecisionClearModel();
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
$knjl770hCtl = new knjl770hController();
