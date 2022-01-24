<?php
require_once('knjl750hModel.inc');
require_once('knjl750hQuery.inc');

class knjl750hController extends Controller
{
    public $ModelClassName = "knjl750hModel";
    public $ProgramID      = "KNJL750H";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "read":
                case "updread":
                case "reset":
                case "end":
                    $this->callView("knjl750hForm1");
                    break 2;
                case "update":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("updread");
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
$knjl750hCtl = new knjl750hController;
