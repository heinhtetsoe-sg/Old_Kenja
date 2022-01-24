<?php

require_once('for_php7.php');

require_once('knjb103dModel.inc');
require_once('knjb103dQuery.inc');

class knjb103dController extends Controller
{
    public $ModelClassName = "knjb103dModel";
    public $ProgramID      = "KNJB103D";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "change":
                case "knjb103d":
                    $sessionInstance->knjb103dModel();
                    $this->callView("knjb103dForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjb103dCtl = new knjb103dController();
