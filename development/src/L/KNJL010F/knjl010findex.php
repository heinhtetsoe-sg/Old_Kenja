<?php

require_once('for_php7.php');

require_once('knjl010fModel.inc');
require_once('knjl010fQuery.inc');

class knjl010fController extends Controller
{
    public $ModelClassName = "knjl010fModel";
    public $ProgramID      = "KNJL010F";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "changeTest":
                case "showdivAdd":
                case "back1":
                case "next1":
                    $this->callView("knjl010fForm1");
                    break 2;
                case "addnew":
                    $sessionInstance->getMaxExamno();
                    $sessionInstance->setCmd("showdivAdd");
                    break 1;
                case "add":
                    $sessionInstance->getInsertModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "update":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "back":
                    $sessionInstance->getUpdateModel();
                    $this->callView("knjl010fForm1");
                    break 2;
                case "next":
                    $sessionInstance->getUpdateModel();
                    $this->callView("knjl010fForm1");
                    break 2;
                case "delete":
                    $sessionInstance->getDeleteModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "reference":
                    $this->callView("knjl010fForm1");
                    break 2;
                case "reset":
                case "":
                    $sessionInstance->setCmd("main");
                    break 1;
                default:
                    $sessionInstance->setError(new PEAR_Error("???????????????????????????{$sessionInstance->cmd}??????"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl010fCtl = new knjl010fController();
