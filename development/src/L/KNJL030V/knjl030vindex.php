<?php

require_once('for_php7.php');
require_once('knjl030vModel.inc');
require_once('knjl030vQuery.inc');

class knjl030vController extends Controller
{
    public $ModelClassName = "knjl030vModel";
    public $ProgramID      = "KNJL030V";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "main":
                    $this->callView("knjl030vForm1");
                    break 2;
                case "csv":
                    if (!$sessionInstance->getCsvModel()) {
                        $this->callView("knjl030vForm1");
                    }
                    break 2;
                case "exec":
                    $sessionInstance->getExecModel();
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
$knjl030vCtl = new knjl030vController();
