<?php

require_once('for_php7.php');

require_once('knjh400_bukatuModel.inc');
require_once('knjh400_bukatuQuery.inc');

class knjh400_bukatuController extends Controller
{
    public $ModelClassName = "knjh400_bukatuModel";
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
                    $this->callView("knjh400_bukatuForm1");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjh400_bukatuCtl = new knjh400_bukatuController();
