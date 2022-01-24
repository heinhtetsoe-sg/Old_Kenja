<?php

require_once('for_php7.php');

require_once('knjb212Model.inc');
require_once('knjb212Query.inc');

class knjb212Controller extends Controller
{
    public $ModelClassName = "knjb212Model";
    public $ProgramID      = "KNJB212";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjb212":                             //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjb212Model();       //コントロールマスタの呼び出し
                    $this->callView("knjb212Form1");
                    exit;
                case "csv":
                    if (!$sessionInstance->getCsvModel()) {
                        $sessionInstance->setCmd("knjb212");
                        break 1;
                    }
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjb212Ctl = new knjb212Controller();
