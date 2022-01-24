<?php

require_once('for_php7.php');

require_once('knjl151dModel.inc');
require_once('knjl151dQuery.inc');

class knjl151dController extends Controller
{
    public $ModelClassName = "knjl151dModel";
    public $ProgramID      = "KNJL151D";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "read":
                case "read2":
                case "next":
                case "back":
                case "reset":
                case "end":
                    $this->callView("knjl151dForm1");
                    break 2;
                case "update":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("read");
                    break 1;
                case "csv":
                    if (!$sessionInstance->getCsvModel()) {
                        $this->callView("knjl151dForm1");
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
$knjl151dCtl = new knjl151dController();
