<?php

require_once('for_php7.php');

require_once('knje370eModel.inc');
require_once('knje370eQuery.inc');

class knje370eController extends Controller {
    var $ModelClassName = "knje370eModel";
    var $ProgramID      = "KNJE370E";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knje370e":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knje370eModel();        //コントロールマスタの呼び出し
                    $this->callView("knje370eForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knje370eCtl = new knje370eController;
var_dump($_REQUEST);
?>
