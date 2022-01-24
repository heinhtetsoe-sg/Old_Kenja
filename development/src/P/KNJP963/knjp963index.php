<?php

require_once('for_php7.php');

require_once('knjp963Model.inc');
require_once('knjp963Query.inc');

class knjp963Controller extends Controller
{
    public $ModelClassName = "knjp963Model";
    public $ProgramID      = "KNJP963";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjp963":                             //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjp963Model();       //コントロールマスタの呼び出し
                    $this->callView("knjp963Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjp963Ctl = new knjp963Controller();
