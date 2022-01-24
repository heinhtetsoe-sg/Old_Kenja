<?php

require_once('for_php7.php');
require_once('knjh400_allergiesModel.inc');
require_once('knjh400_allergiesQuery.inc');

class knjh400_allergiesController extends Controller
{
    public $ModelClassName = "knjh400_allergiesModel";
    public $ProgramID      = "KNJH400";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "edit":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjh400_allergiesForm1");
                    break 2;
                case "form1":
                case "form1A":
                case "form1_clear":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjh400_allergiesForm1");
                    break 2;
                case "form2":
                case "form2A":
                case "form2_clear":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjh400_allergiesForm2");
                    break 2;
                case "form3":
                case "form3A":
                case "form3_clear":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjh400_allergiesForm3");
                    break 2;
                case "form4":
                case "form4A":
                case "form4_clear":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjh400_allergiesForm4");
                    break 2;
                case "form6":
                case "form6A":
                case "form6_clear":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjh400_allergiesForm6");
                    break 2;
                case "":
                    $sessionInstance->setCmd(VARS::request("cmd"));
                    break 1;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjh400_allergiesCtl = new knjh400_allergiesController();
