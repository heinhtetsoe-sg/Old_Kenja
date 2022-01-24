<?php

require_once('for_php7.php');

require_once('knje364aModel.inc');
require_once('knje364aQuery.inc');

class knje364aController extends Controller
{
    public $ModelClassName = "knje364aModel";
    public $ProgramID      = "KNJE364A";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "exec":    //CSV取込
                    $sessionInstance->getExecModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "csv":     //CSV出力
                    if (!$sessionInstance->getDownloadModel()) {
                        $this->callView("knje364aForm1");
                    }
                    break 2;
                case "":
                case "main":
                    $sessionInstance->knje364aModel();
                    $this->callView("knje364aForm1");
                    break 2;
                case "error":
                    $this->callView("error");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knje364aCtl = new knje364aController();
