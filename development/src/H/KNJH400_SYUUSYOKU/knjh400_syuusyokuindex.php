<?php

require_once('for_php7.php');

require_once('knjh400_syuusyokuModel.inc');
require_once('knjh400_syuusyokuQuery.inc');

class knjh400_syuusyokuController extends Controller
{
    public $ModelClassName = "knjh400_syuusyokuModel";
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
                    $this->callView("knjh400_syuusyokuForm1");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjh400_syuusyokuCtl = new knjh400_syuusyokuController();
