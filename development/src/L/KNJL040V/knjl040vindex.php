<?php
require_once('knjl040vModel.inc');
require_once('knjl040vQuery.inc');

class knjl040vController extends Controller
{
    public $ModelClassName = "knjl040vModel";
    public $ProgramID      = "KNJL040V";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "reset":
                case "end":
                case "back":
                    $this->callView("knjl040vForm1");
                    break 2;
                case "update":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "":
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
$knjl040vCtl = new knjl040vController();
