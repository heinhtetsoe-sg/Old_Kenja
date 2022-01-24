<?php

require_once('knjh442aModel.inc');
require_once('knjh442aQuery.inc');

class knjh442aController extends Controller
{
    public $ModelClassName = "knjh442aModel";
    public $ProgramID      = "KNJH442A";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjh442a":
                case "gakki":
                    $sessionInstance->knjh442aModel();
                    $this->callView("knjh442aForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getCsvModel()) {
                        $this->callView("knjh442aForm1");
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
$knjh442aCtl = new knjh442aController();
