<?php
require_once('knjl693hModel.inc');
require_once('knjl693hQuery.inc');

class knjl693hController extends Controller
{
    public $ModelClassName = "knjl693hModel";
    public $ProgramID      = "KNJL693H";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "calc":
                case "knjl693h":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $sessionInstance->knjl693hModel();
                    $this->callView("knjl693hForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl693hCtl = new knjl693hController();
