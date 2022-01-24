<?php

require_once('for_php7.php');

require_once('knjl432hModel.inc');
require_once('knjl432hQuery.inc');

class knjl432hController extends Controller
{
    public $ModelClassName = "knjl432hModel";
    public $ProgramID      = "KNJL432H";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "read":
                case "back":
                case "next":
                case "reset":
                    $sessionInstance->getMainModel();
                    $this->callView("knjl432hForm1");
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
$knjl432hCtl = new knjl432hController();
