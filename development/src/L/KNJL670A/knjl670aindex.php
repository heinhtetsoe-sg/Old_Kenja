<?php

require_once('for_php7.php');

require_once('knjl670aModel.inc');
require_once('knjl670aQuery.inc');

class knjl670aController extends Controller
{
    public $ModelClassName = "knjl670aModel";
    public $ProgramID      = "KNJL670A";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "app":
                case "main":
                case "load":
                case "read":
                case "sort":
                case "back":
                case "next":
                case "reset":
                    $this->callView("knjl670aForm1");
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
$knjl670aCtl = new knjl670aController();
