<?php
require_once('knjl692hModel.inc');
require_once('knjl692hQuery.inc');

class knjl692hController extends Controller
{
    public $ModelClassName = "knjl692hModel";
    public $ProgramID      = "KNJL692H";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl692h":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $sessionInstance->knjl692hModel();
                    $this->callView("knjl692hForm1");
                    exit;
                case "csv":
                    if (!$sessionInstance->getDownloadCsvModel()) {
                        $this->callView("knjl692hForm1");
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
$knjl692hCtl = new knjl692hController();
