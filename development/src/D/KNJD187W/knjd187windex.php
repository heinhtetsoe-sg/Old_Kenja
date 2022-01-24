<?php

require_once('for_php7.php');
require_once('knjd187wModel.inc');
require_once('knjd187wQuery.inc');

class knjd187wController extends Controller
{
    public $ModelClassName = "knjd187wModel";
    public $ProgramID      = "KNJD187W";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        $sessionInstance->programID = $this->ProgramID;

        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "change":
                    $this->callView("knjd187wForm1");
                    break 2;
                case "":
                case "main":
                    $this->callView("knjd187wForm1");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd187wCtl = new knjd187wController();
