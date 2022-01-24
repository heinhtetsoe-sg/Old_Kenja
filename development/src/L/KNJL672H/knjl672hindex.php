<?php
require_once('knjl672hModel.inc');
require_once('knjl672hQuery.inc');

class knjl672hController extends Controller
{
    public $ModelClassName = "knjl672hModel";
    public $ProgramID      = "KNJL672H";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "csv":
                case "knjl672h":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $sessionInstance->knjl672hModel();
                    $this->callView("knjl672hForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl672hCtl = new knjl672hController();
