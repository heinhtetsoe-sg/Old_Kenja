<?php

require_once('for_php7.php');

require_once('knje130jModel.inc');
require_once('knje130jQuery.inc');

class knje130jController extends Controller {
    var $ModelClassName = "knje130jModel";
    var $ProgramID      = "KNJE130J";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knje130j":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knje130jModel();        //コントロールマスタの呼び出し
                    $this->callView("knje130jForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knje130jCtl = new knje130jController;
//var_dump($_REQUEST);
?>
