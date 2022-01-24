<?php

require_once('for_php7.php');

require_once('knjd280dModel.inc');
require_once('knjd280dQuery.inc');

class knjd280dController extends Controller {
    var $ModelClassName = "knjd280dModel";
    var $ProgramID      = "KNJD280D";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd280d":                            //メニュー画面もしくはSUBMITした場合
                case "print":
                    $sessionInstance->knjd280dModel();      //コントロールマスタの呼び出し
                    $this->callView("knjd280dForm1");
                    exit;
                case "clickchange":                         //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjd280dModel();      //コントロールマスタの呼び出し
                    $this->callView("knjd280dForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd280dCtl = new knjd280dController;
?>
