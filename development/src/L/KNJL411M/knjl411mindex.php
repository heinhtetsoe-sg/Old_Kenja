<?php

require_once('for_php7.php');
require_once('knjl411mModel.inc');
require_once('knjl411mQuery.inc');
require_once('../../common/mycalendar.php');

class knjl411mController extends Controller
{
    public $ModelClassName = "knjl411mModel";
    public $ProgramID      = "KNJL411M";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "main":
                    $this->callView("knjl411mForm1");
                    break 2;
                case "csvExec":     //CSV処理 取込
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $sessionInstance->setHeader();
                    $sessionInstance->getExecModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "csvDownload": //CSV処理 データ出力
                case "head":
                    $sessionInstance->setHeader();
                    if (!$sessionInstance->getDownloadModel()) {
                        $this->callView("knjl411mForm1");
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
$knjl411mCtl = new knjl411mController();
