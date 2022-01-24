<?php

require_once('for_php7.php');

require_once('knjl620iModel.inc');
require_once('knjl620iQuery.inc');

class knjl620iController extends Controller
{
    public $ModelClassName = "knjl620iModel";
    public $ProgramID      = "KNJL620I";     //プログラムID

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "main":
                case "edit":
                case "chgMajor":
                case "clearShow":
                    $sessionInstance->getMainModel();
                    $this->callView("knjl620iForm1");
                    break 2;
                case "sim":
                    $sessionInstance->getSimModel();
                    $sessionInstance->setCmd("decision");
                    break 1;
                case "decision":
                    $sessionInstance->getDecisionModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "clear":
                    $sessionInstance->getClearModel();
                    $sessionInstance->setCmd("clearShow");
                    break 1;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl620iCtl = new knjl620iController();
