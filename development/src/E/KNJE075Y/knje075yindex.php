<?php

require_once('for_php7.php');

require_once('knje075yModel.inc');
require_once('knje075yQuery.inc');

class knje075yController extends Controller {
    var $ModelClassName = "knje075yModel";
    var $ProgramID      = "KNJE075Y";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knje075y":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knje075yModel();      //コントロールマスタの呼び出し
                    $this->callView("knje075yForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knje075yCtl = new knje075yController;
//var_dump($_REQUEST);
?>
