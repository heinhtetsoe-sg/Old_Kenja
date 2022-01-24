<?php

require_once('for_php7.php');

require_once('knjc201Model.inc');
require_once('knjc201Query.inc');

class knjc201Controller extends Controller
{
    public $ModelClassName = "knjc201Model";
    public $ProgramID      = "KNJC201";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "output":
                case "knjc201":                             //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjc201Model();       //コントロールマスタの呼び出し
                    $this->callView("knjc201Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjc201Ctl = new knjc201Controller();
