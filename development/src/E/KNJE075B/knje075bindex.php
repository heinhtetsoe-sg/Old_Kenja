<?php

require_once('for_php7.php');

require_once('knje075bModel.inc');
require_once('knje075bQuery.inc');

class knje075bController extends Controller
{
    public $ModelClassName = "knje075bModel";
    public $ProgramID      = "KNJE075B";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knje075b":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knje075bModel();      //コントロールマスタの呼び出し
                    $this->callView("knje075bForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knje075bCtl = new knje075bController;
//var_dump($_REQUEST);
