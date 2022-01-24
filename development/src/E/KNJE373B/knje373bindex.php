<?php

require_once('for_php7.php');

require_once('knje373bModel.inc');
require_once('knje373bQuery.inc');

class knje373bController extends Controller
{
    public $ModelClassName = "knje373bModel";
    public $ProgramID      = "KNJE373B";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "change":
                case "knje373b":
                    $sessionInstance->knje373bModel();
                    $this->callView("knje373bForm1");
                    exit;
                case "csv":
                    if (!$sessionInstance->getDownloadModel()) {
                        $this->callView("knje373bForm1");
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
$knje373bCtl = new knje373bController();
