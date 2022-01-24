<?php

require_once('for_php7.php');

require_once('knje071Model.inc');
require_once('knje071Query.inc');

class knje071Controller extends Controller
{
    public $ModelClassName = "knje071Model";
    public $ProgramID      = "KNJE071";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "print":
                case "knje071":                             //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $sessionInstance->knje071Model();       //コントロールマスタの呼び出し
                    $this->callView("knje071Form1");
                    exit;
                case "update":     //発行
                    $sessionInstance->setAccessLogDetail("EO", $ProgramID);
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd('print');
                    break 1;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knje071Ctl = new knje071Controller();
//var_dump($_REQUEST);
