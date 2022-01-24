<?php

require_once('for_php7.php');
require_once('knjh562bModel.inc');
require_once('knjh562bQuery.inc');

class knjh562bController extends Controller
{
    public $ModelClassName = "knjh562bModel";
    public $ProgramID      = "KNJH562B";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjh562b":                             //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjh562bModel();       //コントロールマスタの呼び出し
                    $this->callView("knjh562bForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjh562bCtl = new knjh562bController();
