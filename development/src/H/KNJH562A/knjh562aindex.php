<?php

require_once('for_php7.php');
require_once('knjh562aModel.inc');
require_once('knjh562aQuery.inc');

class knjh562aController extends Controller
{
    public $ModelClassName = "knjh562aModel";
    public $ProgramID      = "KNJH562A";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        $sessionInstance->programID = $this->ProgramID;

        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "main":
                case "change_prfcencydiv":
                case "change_prfcencycd":
                case "change_row":
                case "cancel":
                    $this->callView("knjh562aForm1");
                    break 2;
                case "update":
                    $this->checkAuth(DEF_UPDATABLE);
                    if (!$sessionInstance->updateLimitScore()) {
                        $this->callView("knjh562aForm1");
                    }
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjh562aCtl = new knjh562aController();
