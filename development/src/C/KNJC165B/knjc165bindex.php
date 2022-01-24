<?php

require_once('for_php7.php');

require_once('knjc165bModel.inc');
require_once('knjc165bQuery.inc');

class knjc165bController extends Controller
{
    public $ModelClassName = "knjc165bModel";
    public $ProgramID      = "KNJC165B";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjc165b":                             //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjc165bModel();       //コントロールマスタの呼び出し
                    $this->callView("knjc165bForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjc165bCtl = new knjc165bController();
