<?php

require_once('for_php7.php');

require_once('knje373hModel.inc');
require_once('knje373hQuery.inc');

class knje373hController extends Controller
{
    public $ModelClassName = "knje373hModel";
    public $ProgramID      = "KNJE373H";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knje373h":
                    $sessionInstance->knje373hModel();
                    $this->callView("knje373hForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knje373hCtl = new knje373hController();
