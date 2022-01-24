<?php

require_once('for_php7.php');

require_once('knjz100aModel.inc');
require_once('knjz100aQuery.inc');

class knjz100aController extends Controller
{
    public $ModelClassName = "knjz100aModel";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjz100a":                             //メニュー画面もしくはSUBMITした場合
                case "knjz100achangeDiv":
                    $sessionInstance->knjz100aModel();       //コントロールマスタの呼び出し
                    $this->callView("knjz100aForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjz100aCtl = new knjz100aController();
