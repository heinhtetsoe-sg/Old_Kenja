<?php
require_once('knjl120iModel.inc');
require_once('knjl120iQuery.inc');

class knjl120iController extends Controller
{
    public $ModelClassName = "knjl120iModel";
    public $ProgramID      = "KNJL120I";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "reset":
                case "end":
                case "search":
                case "back":
                case "next":
                case "changeDispDiv":
                case "getPointMst":
                    $this->callView("knjl120iForm1");
                    break 2;
                case "update":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("search");
                    break 1;
                case "exec":
                    $sessionInstance->getExecModel();
                    $sessionInstance->setCmd("search");
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
$knjl120iCtl = new knjl120iController();
