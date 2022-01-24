<?php

require_once('for_php7.php');

require_once('knja226bModel.inc');
require_once('knja226bQuery.inc');

class knja226bController extends Controller
{
    public $ModelClassName = "knja226bModel";
    public $ProgramID      = "KNJA226B";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knja226b":
                    $sessionInstance->knja226bModel();
                    $this->callView("knja226bForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knja226bCtl = new knja226bController();
