<?php

require_once('for_php7.php');

require_once('knjc035fModel.inc');
require_once('knjc035fQuery.inc');

class knjc035fController extends Controller
{
    public $ModelClassName = "knjc035fModel";
    public $ProgramID      = "KNJC035F";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "reset":
                    $this->callView("knjc035fForm1");
                    break 2;
                case "change":
                case "schoolkind":
                case "course":
                case "subclasscd":
                case "chaircd":
                    $this->callView("knjc035fForm1");
                    break 2;
                case "update":
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
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
$knjc035fCtl = new knjc035fController();
