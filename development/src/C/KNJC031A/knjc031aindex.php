<?php

require_once('for_php7.php');

require_once('knjc031aModel.inc');
require_once('knjc031aQuery.inc');

class knjc031aController extends Controller
{
    public $ModelClassName = "knjc031aModel";
    public $ProgramID      = "KNJC031A";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "change":
                case "reset":
                    $this->callView("knjc031aForm1");
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

$knjc031aCtl = new knjc031aController();
