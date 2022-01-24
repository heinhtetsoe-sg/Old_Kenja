<?php

require_once('for_php7.php');

require_once('knja082uModel.inc');
require_once('knja082uQuery.inc');

class knja082uController extends Controller {
    var $ModelClassName = "knja082uModel";
    var $ProgramID      = "KNJA082U";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knja082u":                              //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knja082uModel();        //コントロールマスタの呼び出し
                    $this->callView("knja082uForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knja082uCtl = new knja082uController;
?>
