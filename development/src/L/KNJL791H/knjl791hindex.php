<?php
require_once('knjl791hModel.inc');
require_once('knjl791hQuery.inc');

class knjl791hController extends Controller
{
    public $ModelClassName = "knjl791hModel";
    public $ProgramID      = "KNJL791H";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "main":
                    $this->callView("knjl791hForm1");
                    break 2;
                case "csvOutput":   //CSV出力
                    if (!$sessionInstance->getCsvModel()) {
                        $this->callView("knjl791hForm1");
                    }
                    $sessionInstance->setAccessLogDetail("EO", $ProgramID);
                    break 2;
                case "error":
                    $this->callView("error");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl791hCtl = new knjl791hController;
