<?php

require_once('for_php7.php');

require_once('knjc110dModel.inc');
require_once('knjc110dQuery.inc');

class knjc110dController extends Controller
{
    public $ModelClassName = "knjc110dModel";
    public $ProgramID      = "KNJC110D";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjc110d":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $sessionInstance->knjc110dModel();
                    $this->callView("knjc110dForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjc110dCtl = new knjc110dController();
