<?php

require_once('for_php7.php');

require_once('knjf341aModel.inc');
require_once('knjf341aQuery.inc');

class knjf341aController extends Controller
{
    public $ModelClassName = "knjf341aModel";
    public $ProgramID      = "KNJF341A";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjf341a":
                    $sessionInstance->knjf341aModel();
                    $this->callView("knjf341aForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()) {
                        $this->callView("knjf341aForm1");
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
$knjf341aCtl = new knjf341aController();
