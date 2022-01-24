<?php

require_once('for_php7.php');

require_once('knjf102aModel.inc');
require_once('knjf102aQuery.inc');

class knjf102aController extends Controller
{
    public $ModelClassName = "knjf102aModel";
    public $ProgramID      = "KNJF102A";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjf102a":                            //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $sessionInstance->knjf102aModel();      //コントロールマスタの呼び出し
                    $this->callView("knjf102aForm1");
                    exit;
                case "clickchange":                         //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $sessionInstance->knjf102aModel();      //コントロールマスタの呼び出し
                    $this->callView("knjf102aForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjf102aCtl = new knjf102aController();
