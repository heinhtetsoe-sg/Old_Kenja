<?php

require_once('for_php7.php');

require_once('knjl050dModel.inc');
require_once('knjl050dQuery.inc');

class knjl050dController extends Controller
{
    public $ModelClassName = "knjl050dModel";
    public $ProgramID      = "KNJL050D";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "read":
                case "next":
                case "back":
                case "reset":
                case "end":
                    $this->callView("knjl050dForm1");
                    break 2;
                case "update":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("read");
                    break 1;
                case "csv":
                    if (!$sessionInstance->getCsvModel()) {
                        $this->callView("knjl050dForm1");
                    }
                    break 2;
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
$knjl050dCtl = new knjl050dController();
