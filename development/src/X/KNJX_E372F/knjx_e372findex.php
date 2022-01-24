<?php

require_once('knjx_e372fModel.inc');
require_once('knjx_e372fQuery.inc');

class knjx_e372fController extends Controller
{
    public $ModelClassName = "knjx_e372fModel";
    public $ProgramID      = "KNJX_E372F";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "main":
                    $this->callView("knjx_e372fForm1");
                    break 2;
                case "csvExec":     //CSV処理 取込
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $sessionInstance->getCsvExecModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "csvDownload": //CSV処理 データ出力
                    if (!$sessionInstance->getDownloadModel()) {
                        $this->callView("knjx_e372fForm1");
                    }
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
$knjx_e372fCtl = new knjx_e372fController();
