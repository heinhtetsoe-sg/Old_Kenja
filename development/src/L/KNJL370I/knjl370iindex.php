<?php
require_once('knjl370iModel.inc');
require_once('knjl370iQuery.inc');

class knjl370iController extends Controller
{
    public $ModelClassName = "knjl370iModel";
    public $ProgramID      = "KNJL370I";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl370i":
                    $sessionInstance->setAccessLogDetail("S", $this->ProgramID);
                    $sessionInstance->knjl370iModel();
                    $this->callView("knjl370iForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl370iCtl = new knjl370iController();
