<?php

require_once('for_php7.php');

require_once('knje372aModel.inc');
require_once('knje372aQuery.inc');

class knje372aController extends Controller {
    var $ModelClassName = "knje372aModel";
    var $ProgramID      = "KNJE372A";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knje372a":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knje372aModel();        //コントロールマスタの呼び出し
                    $this->callView("knje372aForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knje372aCtl = new knje372aController;
var_dump($_REQUEST);
?>
