<?php
require_once('knjl530iModel.inc');
require_once('knjl530iQuery.inc');

class knjl530iController extends Controller
{
    public $ModelClassName = "knjl530iModel";
    public $ProgramID      = "KNJL530I";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                    $sessionInstance->getMainModel();
                    $this->callView("knjl530iForm1");
                    break 2;
                case "edit":
                    $this->callView("knjl530iForm2");
                    break 2;
                case "insert":
                    $sessionInstance->getInsertModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "update":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("edit");
                    break 1;
                case "exec":
                    $sessionInstance->getExecModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "delete":
                    $sessionInstance->getDeleteModel();
                    $sessionInstance->setCmd("main");
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
$knjl530iCtl = new knjl530iController;
?>
