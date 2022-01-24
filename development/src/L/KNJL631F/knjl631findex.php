<?php
require_once('knjl631fModel.inc');
require_once('knjl631fQuery.inc');

class knjl631fController extends Controller
{
    public $ModelClassName = "knjl631fModel";
    public $ProgramID      = "KNJL631F";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "main":
                case "knjl631f":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $sessionInstance->knjl631fModel();
                    $this->callView("knjl631fForm1");
                    exit;
                case "csvInput":    //CSV取込
                    $sessionInstance->setAccessLogDetail("E", $ProgramID);
                    $sessionInstance->getCsvInputModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "csvOutput":
                    $sessionInstance->setAccessLogDetail("EO", $ProgramID);
                    if (!$sessionInstance->getHeaderModel()) {
                        $this->callView("knjl631fForm1");
                    }
                    break 2;
                case "errOutput":
                    $sessionInstance->setAccessLogDetail("EO", $ProgramID);
                    if (!$sessionInstance->getCsvModel()) {
                        $this->callView("knjl631fForm1");
                    }
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl631fCtl = new knjl631fController();
