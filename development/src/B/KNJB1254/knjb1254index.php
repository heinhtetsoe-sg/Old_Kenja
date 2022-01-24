<?php

require_once('for_php7.php');

require_once('knjb1254Model.inc');
require_once('knjb1254Query.inc');

class knjb1254Controller extends Controller
{
    public $ModelClassName = "knjb1254Model";
    public $ProgramID      = "KNJB1254";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjb1254":                         //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjb1254Model();   //コントロールマスタの呼び出し
                    $this->callView("knjb1254Form1");
                    exit;
                case "create":
                    $this->checkAuth(DEF_UPDATABLE);
                    $sessionInstance->setAccessLogDetail("I", $ProgramID);
                    $sessionInstance->getCreateModel();
                    $sessionInstance->setCmd("knjb1254");
                    break 1;
                case "csv":
                    $sessionInstance->setAccessLogDetail("EO", $ProgramID);
                    $sessionInstance->getCsvModel();
                    $sessionInstance->setCmd("knjb1254");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjb1254Ctl = new knjb1254Controller();
