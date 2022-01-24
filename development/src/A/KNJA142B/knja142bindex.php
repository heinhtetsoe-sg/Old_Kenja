<?php

require_once('for_php7.php');

require_once('knja142bModel.inc');
require_once('knja142bQuery.inc');

class knja142bController extends Controller
{
    public $ModelClassName = "knja142bModel";
    public $ProgramID      = "KNJA142B";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "output":
                case "knja142b":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $sessionInstance->knja142bModel();        //コントロールマスタの呼び出し
                    $this->callView("knja142bForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knja142bCtl = new knja142bController();
