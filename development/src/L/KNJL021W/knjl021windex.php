<?php

require_once('for_php7.php');

require_once('knjl021wModel.inc');
require_once('knjl021wQuery.inc');

class knjl021wController extends Controller
{
    public $ModelClassName = "knjl021wModel";
    public $ProgramID      = "KNJL021W";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "keisan":
                case "back1":
                case "next1":
                    $this->callView("knjl021wForm1");
                    break 2;
                case "update":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "reset":
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
$knjl021wCtl = new knjl021wController;
