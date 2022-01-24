<?php

require_once('for_php7.php');

require_once('knjb060bModel.inc');
require_once('knjb060bQuery.inc');

class knjb060bController extends Controller
{
    public $ModelClassName = "knjb060bModel";
    public $ProgramID      = "KNJB060B";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjb060b":
                    $sessionInstance->knjb060bModel();
                    $this->callView("knjb060bForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()) {
                        $this->callView("knjb060bForm1");
                    }
                    $sessionInstance->setAccessLogDetail("EO", $ProgramID);
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjb060bCtl = new knjb060bController();
