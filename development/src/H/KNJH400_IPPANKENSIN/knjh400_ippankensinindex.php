<?php

require_once('for_php7.php');

require_once('knjh400_ippankensinModel.inc');
require_once('knjh400_ippankensinQuery.inc');

class knjh400_ippankensinController extends Controller
{
    public $ModelClassName = "knjh400_ippankensinModel";
    public $ProgramID      = "KNJH400";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "edit":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjh400_ippankensinForm1");
                    break 2;
                case "replace1":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjh400_ippankensinSubForm1");
                    break 2;
                case "replace2":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjh400_ippankensinSubForm2");
                    break 2;
                case "replace3":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjh400_ippankensinSubForm3");
                    break 2;
                case "replace4":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjh400_ippankensinSubForm4");
                    break 2;
                case "reset":
                    $this->callView("knjh400_ippankensinForm1");
                    break 2;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjh400_ippankensinForm1");
                    break 2;
                case "back":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjh400_ippankensinForm1");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjh400_ippankensinCtl = new knjh400_ippankensinController();
