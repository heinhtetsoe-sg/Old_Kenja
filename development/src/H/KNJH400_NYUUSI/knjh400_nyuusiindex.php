<?php

require_once('for_php7.php');

require_once('knjh400_nyuusiModel.inc');
require_once('knjh400_nyuusiQuery.inc');

class knjh400_nyuusiController extends Controller
{
    public $ModelClassName = "knjh400_nyuusiModel";
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
                    $this->callView("knjh400_nyuusiForm1");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjh400_nyuusiCtl = new knjh400_nyuusiController();
