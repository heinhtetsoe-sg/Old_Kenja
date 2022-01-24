<?php

require_once('for_php7.php');

require_once('knje080aModel.inc');
require_once('knje080aQuery.inc');

class knje080aController extends Controller
{
    public $ModelClassName = "knje080aModel";
    public $ProgramID      = "KNJE080A";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knje080a":                          //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $sessionInstance->knje080aModel();    //コントロールマスタの呼び出し
                    $this->callView("knje080aForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knje080aCtl = new knje080aController();
