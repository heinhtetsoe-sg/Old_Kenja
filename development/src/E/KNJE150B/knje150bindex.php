<?php

require_once('for_php7.php');

require_once('knje150bModel.inc');
require_once('knje150bQuery.inc');

class knje150bController extends Controller {
    var $ModelClassName = "knje150bModel";
    var $ProgramID      = "KNJE150B";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knje150b":                            //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knje150bModel();      //コントロールマスタの呼び出し
                    $this->callView("knje150bForm1");
                    exit;
                case "csvOutput":    //CSV出力
                    if (!$sessionInstance->getCsvOutputModel()){
                        $this->callView("knje150bForm1");
                    }
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knje150bCtl = new knje150bController;
?>
