<?php

require_once('for_php7.php');

require_once('knjh400_zyukoukamokuModel.inc');
require_once('knjh400_zyukoukamokuQuery.inc');

class knjh400_zyukoukamokuController extends Controller
{
    public $ModelClassName = "knjh400_zyukoukamokuModel";
    public $ProgramID      = "KNJH400";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "error":
                    $this->callView("error");
                    break 2;
                case "edit":
                case "":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjh400_zyukoukamokuForm1");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjh400_zyukoukamokuCtl = new knjh400_zyukoukamokuController();
