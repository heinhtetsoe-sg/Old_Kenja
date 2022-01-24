<?php

require_once('for_php7.php');

require_once('knje071dModel.inc');
require_once('knje071dQuery.inc');

class knje071dController extends Controller
{
    public $ModelClassName = "knje071dModel";
    public $ProgramID      = "KNJE071D";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "print":
                case "knje071d":                             //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $sessionInstance->knje071dModel();       //コントロールマスタの呼び出し
                    $this->callView("knje071dForm1");
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
$knje071dCtl = new knje071dController();
//var_dump($_REQUEST);
