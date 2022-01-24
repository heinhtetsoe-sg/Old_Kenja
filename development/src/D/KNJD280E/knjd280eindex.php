<?php

require_once('for_php7.php');

require_once('knjd280eModel.inc');
require_once('knjd280eQuery.inc');

class knjd280eController extends Controller {
    var $ModelClassName = "knjd280eModel";
    var $ProgramID      = "KNJD280E";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd280e":                            //メニュー画面もしくはSUBMITした場合
                case "print":
                    $sessionInstance->knjd280eModel();      //コントロールマスタの呼び出し
                    $this->callView("knjd280eForm1");
                    exit;
                case "clickchange":                         //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjd280eModel();      //コントロールマスタの呼び出し
                    $this->callView("knjd280eForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd280eCtl = new knjd280eController;
?>
