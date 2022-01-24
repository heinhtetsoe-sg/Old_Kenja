<?php

require_once('for_php7.php');

require_once('knjd187uModel.inc');
require_once('knjd187uQuery.inc');

class knjd187uController extends Controller
{
    public $ModelClassName = "knjd187uModel";
    public $ProgramID      = "KNJD187U";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd187u":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $sessionInstance->knjd187uModel();        //コントロールマスタの呼び出し
                    $this->callView("knjd187uForm1");
                    exit;
                case "check":
                    $sessionInstance->getCheckModel();
                    $sessionInstance->setCmd("knjd187u");
                    break 1;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd187uCtl = new knjd187uController();
//var_dump($_REQUEST);
