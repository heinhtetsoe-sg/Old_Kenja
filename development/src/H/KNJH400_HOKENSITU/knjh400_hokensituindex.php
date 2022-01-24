<?php

require_once('for_php7.php');

require_once('knjh400_hokensituModel.inc');
require_once('knjh400_hokensituQuery.inc');

class knjh400_hokensituController extends Controller
{
    public $ModelClassName = "knjh400_hokensituModel";
    public $ProgramID      = "KNJH400";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "edit":
                case "clear":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjh400_hokensituForm1");
                    break 2;
                case "subform1":
                case "subform1A":
                case "subform1_clear":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjh400_hokensituSubForm1");
                    break 2;
                case "subform2":
                case "subform2A":
                case "subform2B":
                case "subform2_clear":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjh400_hokensituSubForm2");
                    break 2;
                case "subform3":
                case "subform3A":
                case "subform3_clear":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjh400_hokensituSubForm3");
                    break 2;
                case "subform4":
                case "subform4A":
                case "subform4_clear":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjh400_hokensituSubForm4");
                    break 2;
                case "subform5":
                case "subform5A":
                case "subform5_clear":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjh400_hokensituSubForm5");
                    break 2;
                case "end":
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjh400_hokensituForm1");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjh400_hokensituCtl = new knjh400_hokensituController();
