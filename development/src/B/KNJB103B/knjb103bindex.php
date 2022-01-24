<?php

require_once('for_php7.php');

require_once('knjb103bModel.inc');
require_once('knjb103bQuery.inc');

class knjb103bController extends Controller
{
    public $ModelClassName = "knjb103bModel";
    public $ProgramID      = "KNJB103B";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "change":
                case "knjb103b":
                    $sessionInstance->knjb103bModel();
                    $this->callView("knjb103bForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjb103bCtl = new knjb103bController();
