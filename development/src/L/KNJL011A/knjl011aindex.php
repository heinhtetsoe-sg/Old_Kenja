<?php

require_once('for_php7.php');

require_once('knjl011aModel.inc');
require_once('knjl011aQuery.inc');

class knjl011aController extends Controller
{
    public $ModelClassName = "knjl011aModel";
    public $ProgramID      = "KNJL011A";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "mainAdd":
                case "changeApp":
                case "changeTest":
                case "changeFscd":
                case "back1":
                case "next1":
                case "receptList":
                case "addnew":
                    $this->callView("knjl011aForm1");
                    break 2;
                case "add":
                    $sessionInstance->getInsertModel();
                    $sessionInstance->setCmd("mainAdd");
                    break 1;
                case "update":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "back":
                    $sessionInstance->getUpdateModel();
                    $this->callView("knjl011aForm1");
                    break 2;
                case "next":
                    $sessionInstance->getUpdateModel();
                    $this->callView("knjl011aForm1");
                    break 2;
                case "delete":
                    $sessionInstance->getDeleteModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "add2":
                    $sessionInstance->getInsertModel2();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "update2":
                    $sessionInstance->getUpdateModel2();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "delete2":
                    $sessionInstance->getDeleteModel2();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "reference":
                case "reference2":
                    $this->callView("knjl011aForm1");
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
$knjl011aCtl = new knjl011aController();
