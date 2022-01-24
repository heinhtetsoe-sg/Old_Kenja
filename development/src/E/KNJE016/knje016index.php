<?php

require_once('for_php7.php');

require_once('knje016Model.inc');
require_once('knje016Query.inc');

class knje016Controller extends Controller
{
    public $ModelClassName = "knje016Model";
    public $ProgramID      = "KNJE016";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knje016":
                    $sessionInstance->knje016Model();
                    $this->callView("knje016Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knje016Ctl = new knje016Controller();
