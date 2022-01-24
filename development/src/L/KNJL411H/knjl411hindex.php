<?php

require_once('knjl411hModel.inc');
require_once('knjl411hQuery.inc');
require_once('../../common/mycalendar.php');

class knjl411hController extends Controller
{
    public $ModelClassName = "knjl411hModel";
    public $ProgramID      = "KNJL411H";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "main":
                    $this->callView("knjl411hForm1");
                    break 2;
                case "csvExec":     //CSV処理 取込
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $sessionInstance->getExecModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "csvDownload": //CSV処理 データ出力
                    if (!$sessionInstance->getDownloadModel()) {
                        $this->callView("knjl411hForm1");
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
$knjl411hCtl = new knjl411hController;
