<?php

require_once('for_php7.php');

require_once('knjp964Model.inc');
require_once('knjp964Query.inc');

class knjp964Controller extends Controller
{
    public $ModelClassName = "knjp964Model";
    public $ProgramID      = "KNJP964";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjp964":                       //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjp964Model(); //コントロールマスタの呼び出し
                    $this->callView("knjp964Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjp964Ctl = new knjp964Controller();
