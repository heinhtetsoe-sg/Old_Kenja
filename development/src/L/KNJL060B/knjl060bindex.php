<?php

require_once('for_php7.php');

require_once('knjl060bModel.inc');
require_once('knjl060bQuery.inc');

class knjl060bController extends Controller
{
    public $ModelClassName = "knjl060bModel";
    public $ProgramID      = "KNJL060B";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "exec":
                    $sessionInstance->getExecModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "":
                case "main":
                    $sessionInstance->getMainModel();
                    $this->callView("knjl060bForm1");
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
$knjl060bCtl = new knjl060bController();
