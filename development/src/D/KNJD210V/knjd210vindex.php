<?php

require_once('for_php7.php');

require_once('knjd210vModel.inc');
require_once('knjd210vQuery.inc');

class knjd210vController extends Controller
{
    public $ModelClassName = "knjd210vModel";
    public $ProgramID      = "KNJD210V";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "execute":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $sessionInstance->getExecModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjd210vForm1");
                    break 2;
                case "main":
                    $this->callView("knjd210vForm1");
                    break 2;
                case "error":
                    $this->callView("error");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd210vCtl = new knjd210vController();
