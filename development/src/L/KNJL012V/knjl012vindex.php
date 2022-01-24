<?php

require_once('for_php7.php');

require_once('knjl012vModel.inc');
require_once('knjl012vQuery.inc');

class knjl012vController extends Controller
{
    public $ModelClassName = "knjl012vModel";
    public $ProgramID      = "KNJL011V";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "reset":
                case "iniedit":
                case "edit":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjl012vForm2");
                    break 2;
                case "add":
                case "update":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getUpdateModel();
                    $this->callView("knjl012vForm2");
                    break 2;
                case "delete":
                    $sessionInstance->setAccessLogDetail("D", $ProgramID);
                    $this->checkAuth(DEF_UPDATE_RESTRICT);
                    $sessionInstance->getDeleteModel();
                    $this->callView("knjl012vForm2");
                    break 2;
                case "reset":
                    $this->callView("knjl012vForm1");
                    break 2;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                case "back":
                    //分割フレーム作成
                    $args["left_src"] = "knjl012vindex.php?cmd=init";
                    $args["right_src"] = "knjl012vindex.php?cmd=edit";
                    $args["cols"] = "19%,81%";
                    View::frame($args);
                    return;
                case "move":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjl012vForm2");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl012vCtl = new knjl012vController();
