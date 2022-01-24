<?php

require_once('knjl660hModel.inc');
require_once('knjl660hQuery.inc');

class knjl660hController extends Controller
{
    public $ModelClassName = "knjl660hModel";
    public $ProgramID      = "KNJL660H";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "main":
                case "chgAppDiv":
                    $this->callView("knjl660hForm1");
                    break 2;
                case "exec":
                    $sessionInstance->getExecModel();
                    $sessionInstance->setCmd("main");
                    break 1;
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
$knjl660hCtl = new knjl660hController;
