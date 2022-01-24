<?php

require_once('for_php7.php');

require_once('knje371dModel.inc');
require_once('knje371dQuery.inc');

class knje371dController extends Controller
{
    public $ModelClassName = "knje371dModel";
    public $ProgramID      = "KNJE371D";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knje371d":                             //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knje371dModel();       //コントロールマスタの呼び出し
                    $this->callView("knje371dForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knje371dCtl = new knje371dController();
//var_dump($_REQUEST);
