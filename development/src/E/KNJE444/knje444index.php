<?php

require_once('for_php7.php');

require_once('knje444Model.inc');
require_once('knje444Query.inc');

class knje444Controller extends Controller
{
    public $ModelClassName = "knje444Model";
    public $ProgramID      = "KNJE444";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "main":
                    $this->callView("knje444Form1");
                    break 2;
                case "csv":
                case "houkoku":
                    if (!$sessionInstance->getDownloadCsvOrUpdateHoukokuModel()) {
                        $this->callView("knje444Form1");
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
$knje444Ctl = new knje444Controller();
