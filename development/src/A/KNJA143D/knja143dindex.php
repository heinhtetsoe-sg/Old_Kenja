<?php

require_once('for_php7.php');
require_once('knja143dModel.inc');
require_once('knja143dQuery.inc');

class knja143dController extends Controller
{
    public $ModelClassName = "knja143dModel";
    public $ProgramID      = "KNJA143D";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "output":
                case "knja143d":
                case "change":                              //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knja143dModel();      //コントロールマスタの呼び出し
                    $this->callView("knja143dForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knja143dCtl = new knja143dController();
//var_dump($_REQUEST);
