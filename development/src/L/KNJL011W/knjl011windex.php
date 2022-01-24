<?php

require_once('for_php7.php');

require_once('knjl011wModel.inc');
require_once('knjl011wQuery.inc');

class knjl011wController extends Controller
{
    public $ModelClassName = "knjl011wModel";
    public $ProgramID      = "KNJL011W";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "backFinCopy":
                case "changeTest":
                case "changeCourse":
                case "showdivAdd":
                case "back1":
                case "next1":
                    $this->callView("knjl011wForm1");
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
                    $this->callView("knjl011wForm1");
                    break 2;
                case "next":
                    $sessionInstance->getUpdateModel();
                    $this->callView("knjl011wForm1");
                    break 2;
                case "delete":
                    $sessionInstance->getDeleteModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "reference":
                    $this->callView("knjl011wForm1");
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
$knjl011wCtl = new knjl011wController;
