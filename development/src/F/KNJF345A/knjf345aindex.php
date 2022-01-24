<?php

require_once('for_php7.php');

require_once('knjf345aModel.inc');
require_once('knjf345aQuery.inc');

class knjf345aController extends Controller
{
    public $ModelClassName = "knjf345aModel";
    public $ProgramID      = "KNJF345A";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjf345a":
                    $sessionInstance->knjf345aModel();
                    $this->callView("knjf345aForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()) {
                        $this->callView("knjf345aForm1");
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
$knjf345aCtl = new knjf345aController();
