<?php

require_once('for_php7.php');

require_once('knjl458hModel.inc');
require_once('knjl458hQuery.inc');

class knjl458hController extends Controller
{
    public $ModelClassName = "knjl458hModel";
    public $ProgramID      = "KNJL458H";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "reset":
                case "end":
                    $this->callView("knjl458hForm1");
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
$knjl458hCtl = new knjl458hController();
