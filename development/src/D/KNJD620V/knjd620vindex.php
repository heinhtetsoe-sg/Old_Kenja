<?php

require_once('for_php7.php');

require_once('knjd620vModel.inc');
require_once('knjd620vQuery.inc');

class knjd620vController extends Controller
{
    public $ModelClassName = "knjd620vModel";
    public $ProgramID      = "KNJD620V";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                    $sessionInstance->knjd620vModel();
                    $this->callView("knjd620vForm1");
                    exit;
                case "knjd620v":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $sessionInstance->knjd620vModel();
                    $this->callView("knjd620vForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd620vCtl = new knjd620vController();
