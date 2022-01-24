<?php
require_once('knjl591iModel.inc');
require_once('knjl591iQuery.inc');

class knjl591iController extends Controller
{
    public $ModelClassName = "knjl591iModel";
    public $ProgramID      = "KNJL591I";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "reset":
                case "end":
                case "huban":
                    $this->callView("knjl591iForm1");
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
$knjl591iCtl = new knjl591iController;
?>
