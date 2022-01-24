<?php
require_once('knjl630fModel.inc');
require_once('knjl630fQuery.inc');

class knjl630fController extends Controller
{
    public $ModelClassName = "knjl630fModel";
    public $ProgramID      = "KNJL630F";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "main":
                case "knjl630f":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $sessionInstance->knjl630fModel();
                    $this->callView("knjl630fForm1");
                    exit;
                case "csvInput":    //CSV取込
                    $sessionInstance->setAccessLogDetail("E", $ProgramID);
                    $sessionInstance->getCsvInputModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "csvOutput":
                    $sessionInstance->setAccessLogDetail("EO", $ProgramID);
                    if (!$sessionInstance->getHeaderModel()) {
                        $this->callView("knjl630fForm1");
                    }
                    break 2;
                case "errOutput":
                    $sessionInstance->setAccessLogDetail("EO", $ProgramID);
                    if (!$sessionInstance->getCsvModel()) {
                        $this->callView("knjl630fForm1");
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
$knjl630fCtl = new knjl630fController();
