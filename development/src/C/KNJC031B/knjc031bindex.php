<?php

require_once('for_php7.php');

require_once('knjc031bModel.inc');
require_once('knjc031bQuery.inc');

class knjc031bController extends Controller
{
    public $ModelClassName = "knjc031bModel";
    public $ProgramID      = "KNJC031B";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "change":
                case "reset":
                    $this->callView("knjc031bForm1");
                    break 2;
                case "update":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
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

$knjc031bCtl = new knjc031bController();
