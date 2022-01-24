<?php

require_once('for_php7.php');

require_once('knjg050Model.inc');
require_once('knjg050Query.inc');

class knjg050Controller extends Controller
{
    public $ModelClassName = "knjg050Model";
    public $ProgramID      = "KNJG050";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "cmbclass": //メニュー画面もしくはSUBMITした場合
                case "knjg050":  //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $sessionInstance->knjg050Model(); //コントロールマスタの呼び出し
                    $this->callView("knjg050Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjg050Ctl = new knjg050Controller();
var_dump($_REQUEST);
