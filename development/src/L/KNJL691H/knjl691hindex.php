<?php
require_once('knjl691hModel.inc');
require_once('knjl691hQuery.inc');

class knjl691hController extends Controller
{
    public $ModelClassName = "knjl691hModel";
    public $ProgramID      = "KNJL691H";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "reset":
                case "end":
                case "huban":
                    $this->callView("knjl691hForm1");
                    break 2;
                case "update":
                    $sessionInstance->getUpdateModel();
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
$knjl691hCtl = new knjl691hController;
?>
