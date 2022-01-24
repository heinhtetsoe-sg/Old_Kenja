<?php
require_once('knjl661hModel.inc');
require_once('knjl661hQuery.inc');

class knjl661hController extends Controller
{
    public $ModelClassName = "knjl661hModel";
    public $ProgramID      = "KNJL661H";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "read":
                case "reset":
                case "end":
                    $sessionInstance->getMainModel();
                    $this->callView("knjl661hForm1");
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
$knjl661hCtl = new knjl661hController;
?>
