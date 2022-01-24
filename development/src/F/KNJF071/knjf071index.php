<?php

require_once('for_php7.php');

require_once('knjf071Model.inc');
require_once('knjf071Query.inc');

class knjf071Controller extends Controller
{
    public $ModelClassName = "knjf071Model";
    public $ProgramID      = "KNJF071";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjf071":                              //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjf071Model();        //コントロールマスタの呼び出し
                    $this->callView("knjf071Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjf071Ctl = new knjf071Controller();
