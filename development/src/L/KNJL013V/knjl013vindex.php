<?php

require_once('for_php7.php');

require_once('knjl013vModel.inc');
require_once('knjl013vQuery.inc');

class knjl013vController extends Controller
{
    public $ModelClassName = "knjl013vModel";
    public $ProgramID      = "KNJL013V";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "list":
                case "edit":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjl013vForm1");
                    break 2;
                case "update":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getUpdateModel();
                    $this->callView("knjl013vForm1");
                    break 2;
                case "reset":
                    $this->callView("knjl013vForm1");
                    break 2;
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
$knjl013vCtl = new knjl013vController();
