<?php

require_once('for_php7.php');

require_once('knje130tModel.inc');
require_once('knje130tQuery.inc');

class knje130tController extends Controller {
    var $ModelClassName = "knje130tModel";
    var $ProgramID      = "KNJE130T";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knje130t":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knje130tModel();        //コントロールマスタの呼び出し
                    $this->callView("knje130tForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knje130tCtl = new knje130tController;
var_dump($_REQUEST);
?>
