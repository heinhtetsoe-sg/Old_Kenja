<?php

require_once('for_php7.php');

require_once('knje065bModel.inc');
require_once('knje065bQuery.inc');

class knje065bController extends Controller
{
    public $ModelClassName = "knje065bModel";
    public $ProgramID      = "KNJE065B";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "updMain":
                case "main":
                case "reset":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knje065bForm1");
                    break 2;
                case "recalc":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knje065bForm1");
                    break 2;
                case "update":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("updMain");
                    break 1;
                case "error":
                    $this->callView("error");
                    // no break
                case "read":
                    $sessionInstance->setCmd("main");
                    break 1;
                case "":
                    $sessionInstance->setCmd("main");
                    break 1;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knje065bCtl = new knje065bController();
//var_dump($_REQUEST);
