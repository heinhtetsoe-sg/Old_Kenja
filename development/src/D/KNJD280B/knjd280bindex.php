<?php

require_once('for_php7.php');

require_once('knjd280bModel.inc');
require_once('knjd280bQuery.inc');

class knjd280bController extends Controller {
    var $ModelClassName = "knjd280bModel";
    var $ProgramID      = "KNJD280B";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd280b":                            //メニュー画面もしくはSUBMITした場合
                case "print":
                    $sessionInstance->knjd280bModel();      //コントロールマスタの呼び出し
                    $this->callView("knjd280bForm1");
                    exit;
                case "clickchange":                         //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjd280bModel();      //コントロールマスタの呼び出し
                    $this->callView("knjd280bForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd280bCtl = new knjd280bController;
?>
