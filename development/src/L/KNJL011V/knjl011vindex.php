<?php

require_once('for_php7.php');

require_once('knjl011vModel.inc');
require_once('knjl011vQuery.inc');

class knjl011vController extends Controller
{
    public $ModelClassName = "knjl011vModel";
    public $ProgramID      = "KNJL011V";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "init":
                case "left":
                case "search":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjl011vForm1");
                    break 2;
                case "finschoolsearch":
                case "edit":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjl011vForm2");
                    break 2;
                case "number":
                case "number2":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjl011vForm3");
                    break 2;
                case "add":
                case "update":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getUpdateModel();
                    $this->callView("knjl011vForm2");
                    break 2;
                case "receptUpdate":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getReceptUpdateModel();
                    $this->callView("knjl011vForm3");
                    break 2;
                case "delete":
                    $sessionInstance->setAccessLogDetail("D", $ProgramID);
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getDeleteModel();
                    $this->callView("knjl011vForm2");
                    break 2;
                case "reset":
                    $this->callView("knjl011vForm1");
                    break 2;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                case "move":
                case "back":
                    //分割フレーム作成
                    $args["left_src"] = "knjl011vindex.php?cmd=init";
                    $args["right_src"] = "knjl011vindex.php?cmd=edit";
                    $args["cols"] = "19%,81%";
                    View::frame($args);
                    return;
                case "move2":
                    //分割フレーム作成
                    $args["left_src"] = "knjl011vindex.php?cmd=search";
                    $args["right_src"] = "knjl011vindex.php?cmd=edit";
                    $args["cols"] = "19%,81%";
                    View::frame($args);
                    return;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl011vCtl = new knjl011vController();
