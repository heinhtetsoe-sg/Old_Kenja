<?php

require_once('for_php7.php');

require_once('knjl431hModel.inc');
require_once('knjl431hQuery.inc');

class knjl431hController extends Controller
{
    public $ModelClassName = "knjl431hModel";
    public $ProgramID      = "KNJL431H";

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
                    $this->callView("knjl431hForm1");
                    break 2;
                case "update":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("read");
                    break 1;
                case "":
                    $this->callView("knjl431hForm1");
                    exit;
//                    $sessionInstance->setCmd("main");
//                    break 1;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl431hCtl = new knjl431hController();
