<?php

require_once('for_php7.php');

require_once('knje070hModel.inc');
require_once('knje070hQuery.inc');

class knje070hController extends Controller
{
    public $ModelClassName = "knje070hModel";
    public $ProgramID      = "KNJE070H";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "output":
                case "knje070h":                          //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $sessionInstance->knje070hModel();    //コントロールマスタの呼び出し
                    $this->callView("knje070hForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knje070hCtl = new knje070hController();
