<?php

require_once('for_php7.php');

require_once('knje075jModel.inc');
require_once('knje075jQuery.inc');

class knje075jController extends Controller {
    var $ModelClassName = "knje075jModel";
    var $ProgramID      = "KNJE075J";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knje075j":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knje075jModel();      //コントロールマスタの呼び出し
                    $this->callView("knje075jForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knje075jCtl = new knje075jController;
//var_dump($_REQUEST);
?>
