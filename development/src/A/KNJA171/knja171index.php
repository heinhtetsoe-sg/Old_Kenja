<?php

require_once('for_php7.php');

require_once('knja171Model.inc');
require_once('knja171Query.inc');

class knja171Controller extends Controller
{
    public $ModelClassName = "knja171Model";
    public $ProgramID      = "KNJA171";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knja171":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $sessionInstance->knja171Model();
                    $this->callView("knja171Form1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getCsvModel()) {
                        $this->callView("knja171Form1");
                    }
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knja171Ctl = new knja171Controller();
