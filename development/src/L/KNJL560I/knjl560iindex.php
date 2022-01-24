<?php

require_once('knjl560iModel.inc');
require_once('knjl560iQuery.inc');

class knjl560iController extends Controller
{
    public $ModelClassName = "knjl560iModel";
    public $ProgramID      = "KNJL560I";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "main":
                case "chgAppDiv":
                case "chgTestDiv":
                            $this->callView("knjl560iForm1");
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
$knjl560iCtl = new knjl560iController;
