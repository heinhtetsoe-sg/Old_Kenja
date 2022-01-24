<?php

require_once('knjl062iModel.inc');
require_once('knjl062iQuery.inc');
require_once('../../common/mycalendar.php');

class knjl062iController extends Controller
{
    public $ModelClassName = "knjl062iModel";
    public $ProgramID      = "KNJL062I";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "main":
                    $this->callView("knjl062iForm1");
                    break 2;
                case "csvExec":     //CSV処理 取込
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $sessionInstance->getCsvExecModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "csvDownload": //CSV処理 データ出力
                    if (!$sessionInstance->getDownloadModel()) {
                        $this->callView("knjl062iForm1");
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
$knjl062iCtl = new knjl062iController;
