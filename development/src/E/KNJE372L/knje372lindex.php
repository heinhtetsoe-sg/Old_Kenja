<?php

require_once('for_php7.php');

require_once('knje372lModel.inc');
require_once('knje372lQuery.inc');

class knje372lController extends Controller {
    var $ModelClassName = "knje372lModel";
    var $ProgramID      = "KNJE372L";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knje372l":                                //メニュー画面もしくはSUBMITした場合
                case "edit":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knje372lModel();        //コントロールマスタの呼び出し
                    $this->callView("knje372lForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knje372lCtl = new knje372lController;
//var_dump($_REQUEST);
?>
