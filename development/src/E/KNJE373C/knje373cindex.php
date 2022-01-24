<?php

require_once('for_php7.php');

require_once('knje373cModel.inc');
require_once('knje373cQuery.inc');

class knje373cController extends Controller
{
    public $ModelClassName = "knje373cModel";
    public $ProgramID      = "KNJE373C";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "change":
                case "knje373c":
                    $sessionInstance->knje373cModel();
                    $this->callView("knje373cForm1");
                    exit;
                case "csv":
                    if (!$sessionInstance->getDownloadModel()) {
                        $this->callView("knje373cForm1");
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
$knje373cCtl = new knje373cController();
