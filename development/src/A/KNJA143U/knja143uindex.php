<?php

require_once('for_php7.php');

require_once('knja143uModel.inc');
require_once('knja143uQuery.inc');

class knja143uController extends Controller
{
    public $ModelClassName = "knja143uModel";
    public $ProgramID      = "KNJA143U";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "change":
                case "knja143u":
                    $sessionInstance->knja143uModel();
                    $this->callView("knja143uForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knja143uCtl = new knja143uController();
