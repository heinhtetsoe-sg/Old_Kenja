<?php

require_once('for_php7.php');

require_once('knjl721hModel.inc');
require_once('knjl721hQuery.inc');

class knjl721hController extends Controller
{
    public $ModelClassName = "knjl721hModel";
    public $ProgramID      = "KNJL721H";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "read":
                case "reset":
                    $sessionInstance->getMainModel();
                    $this->callView("knjl721hForm1");
                    break 2;
                case "update":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("read");
                    break 1;
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
$knjl721hCtl = new knjl721hController();
