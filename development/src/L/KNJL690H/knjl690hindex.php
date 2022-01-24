<?php
require_once('knjl690hModel.inc');
require_once('knjl690hQuery.inc');

class knjl690hController extends Controller
{
    public $ModelClassName = "knjl690hModel";
    public $ProgramID      = "KNJL690H";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "change":
                case "changeTestDiv":
                case "reference":
                case "back1":
                case "next1":
                    $this->callView("knjl690hForm1");
                    break 2;
                case "update":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "next":
                case "back":
                    $sessionInstance->getUpdateModel();
                    $this->callView("knjl690hForm1");
                    break 2;
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
$knjl690hCtl = new knjl690hController;
