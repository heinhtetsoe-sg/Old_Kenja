<?php

require_once('for_php7.php');

require_once('knjl073nModel.inc');
require_once('knjl073nQuery.inc');

class knjl073nController extends Controller
{
    public $ModelClassName = "knjl073nModel";
    public $ProgramID      = "KNJL073N";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "app":
                case "main":
                case "read":
                case "back":
                case "next":
                case "reset":
                    $this->callView("knjl073nForm1");
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
$knjl073nCtl = new knjl073nController();
