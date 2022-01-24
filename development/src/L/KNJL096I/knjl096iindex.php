<?php
require_once('knjl096iModel.inc');
require_once('knjl096iQuery.inc');

class knjl096iController extends Controller
{
    public $ModelClassName = "knjl096iModel";
    public $ProgramID      = "KNJL096I";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "main":
                case "edit":
                case "back":
                case "reset":
                    $this->callView("knjl096iForm1");
                    break 2;
                case "update":
                    $sessionInstance->getUpdateModel();
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
$knjl096iCtl = new knjl096iController();
