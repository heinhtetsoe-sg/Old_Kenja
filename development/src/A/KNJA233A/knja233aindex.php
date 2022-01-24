<?php

require_once('for_php7.php');

require_once('knja233aModel.inc');
require_once('knja233aQuery.inc');

class knja233aController extends Controller
{
    public $ModelClassName = "knja233aModel";
    public $ProgramID      = "KNJA233A";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knja233a":
                case "gakki":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $sessionInstance->knja233aModel();
                    $this->callView("knja233aForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()) {
                        $this->callView("knja233aForm1");
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
$knja233aCtl = new knja233aController();
//var_dump($_REQUEST);
