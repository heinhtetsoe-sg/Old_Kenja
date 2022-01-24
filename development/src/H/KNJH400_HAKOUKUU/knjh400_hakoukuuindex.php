<?php

require_once('for_php7.php');

require_once('knjh400_hakoukuuModel.inc');
require_once('knjh400_hakoukuuQuery.inc');

class knjh400_hakoukuuController extends Controller
{
    public $ModelClassName = "knjh400_hakoukuuModel";
    public $ProgramID      = "KNJH400";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "send":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $sessionInstance->getSendModel();
                    break 2;
                case "edit":
                case "edit2":
                case "change":
                case "subEnd":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjh400_hakoukuuForm1");
                    break 2;
                case "replace":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjh400_hakoukuuSubForm1");
                    break 2;
                case "sisiki":
                case "sisiki2":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjh400_hakoukuuSubForm2");
                    break 2;
                case "reset":
                    $this->callView("knjh400_hakoukuuForm1");
                    break 2;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjh400_hakoukuuForm1");
                    break 2;
                case "back":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjh400_hakoukuuForm1");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjh400_hakoukuuCtl = new knjh400_hakoukuuController();
