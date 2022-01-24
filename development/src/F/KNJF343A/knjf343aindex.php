<?php

require_once('for_php7.php');

require_once('knjf343aModel.inc');
require_once('knjf343aQuery.inc');

class knjf343aController extends Controller
{
    public $ModelClassName = "knjf343aModel";
    public $ProgramID      = "KNJF343A";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjf343a":
                    $sessionInstance->knjf343aModel();
                    $this->callView("knjf343aForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()) {
                        $this->callView("knjf343aForm1");
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
$knjf343aCtl = new knjf343aController();
