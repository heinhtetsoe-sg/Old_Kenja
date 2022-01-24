<?php
require_once('knjl810hModel.inc');
require_once('knjl810hQuery.inc');

class knjl810hController extends Controller
{
    public $ModelClassName = "knjl810hModel";
    public $ProgramID      = "KNJL810H";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "main":
                    $this->callView("knjl810hForm1");
                    break 2;
                case "error":
                    $this->callView("error");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl810hCtl = new knjl810hController;
