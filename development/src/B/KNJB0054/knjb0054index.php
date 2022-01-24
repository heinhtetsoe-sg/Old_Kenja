<?php

require_once('for_php7.php');

require_once('knjb0054Model.inc');
require_once('knjb0054Query.inc');

class knjb0054Controller extends Controller
{
    public $ModelClassName = "knjb0054Model";
    public $ProgramID      = "KNJB0054";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjb0054":                         //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjb0054Model();   //コントロールマスタの呼び出し
                    $this->callView("knjb0054Form1");
                    exit;
                case "create":
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->setAccessLogDetail("I", $ProgramID);
                    $sessionInstance->getCreateModel();
                    $sessionInstance->setCmd("knjb0054");
                    break 1;
                case "csv":
                    $sessionInstance->setAccessLogDetail("EO", $ProgramID);
                    $sessionInstance->getCsvModel();
                    $sessionInstance->setCmd("knjb0054");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjb0054Ctl = new knjb0054Controller();
