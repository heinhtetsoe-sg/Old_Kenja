<?php

require_once('for_php7.php');

require_once('knja143jModel.inc');
require_once('knja143jQuery.inc');

class knja143jController extends Controller {
    var $ModelClassName = "knja143jModel";
    var $ProgramID      = "KNJA143J";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "output":
                case "changeOutput":
                case "knja143j":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knja143jModel();      //コントロールマスタの呼び出し
                    $this->callView("knja143jForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knja143jCtl = new knja143jController;
//var_dump($_REQUEST);
?>
