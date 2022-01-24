<?php

require_once('knjx_e372eModel.inc');
require_once('knjx_e372eQuery.inc');

class knjx_e372eController extends Controller
{
    public $ModelClassName = "knjx_e372eModel";
    public $ProgramID      = "KNJX_E372E";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "main":
                    $this->callView("knjx_e372eForm1");
                    break 2;
                case "csvExec":     //CSV処理 取込
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $sessionInstance->getCsvExecModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "csvDownload": //CSV処理 データ出力
                    if (!$sessionInstance->getDownloadModel()) {
                        $this->callView("knjx_e372eForm1");
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
$knjx_e372eCtl = new knjx_e372eController();
