<?php

require_once('for_php7.php');

require_once('knjl050fModel.inc');
require_once('knjl050fQuery.inc');

class knjl050fController extends Controller
{
    public $ModelClassName = "knjl050fModel";
    public $ProgramID      = "KNJL050F";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "end":
                case "main":
                case "read":
                case "back":
                case "next":
                case "reset":
                    $this->callView("knjl050fForm1");
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
$knjl050fCtl = new knjl050fController();
