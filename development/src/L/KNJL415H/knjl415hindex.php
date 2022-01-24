<?php

require_once('for_php7.php');
require_once('knjl415hModel.inc');
require_once('knjl415hQuery.inc');

class knjl415hController extends Controller
{
    public $ModelClassName = "knjl415hModel";
    public $ProgramID      = "KNJL415H";
    public function main()
    {
        $sessionInstance =& Model::getModel();
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "changeApp":
                case "read":
                case "reset":
                case "back":
                case "next":
                case "now":
                    $sessionInstance->getMainModel();
                    $this->callView("knjl415hForm1");
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
$knjl415hCtl = new knjl415hController();
//var_dump($_REQUEST);
