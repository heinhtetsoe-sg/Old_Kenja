<?php

require_once('for_php7.php');

require_once('knjf150fModel.inc');
require_once('knjf150fQuery.inc');

class knjf150fController extends Controller
{
    public $ModelClassName = "knjf150fModel";
    public $ProgramID      = "KNJF150F";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjf150f":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $sessionInstance->knjf150fModel();
                    $this->callView("knjf150fForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjf150fCtl = new knjf150fController();
