<?php

require_once('for_php7.php');

require_once('knje364cModel.inc');
require_once('knje364cQuery.inc');

class knje364cController extends Controller {
    var $ModelClassName = "knje364cModel";
    var $ProgramID      = "KNJE364C";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knje364c":                            //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knje364cModel();      //コントロールマスタの呼び出し
                    $this->callView("knje364cForm1");
                    exit;
                case "csvOutput":    //CSV出力
                    if (!$sessionInstance->getCsvOutputModel()){
                        $this->callView("knje364cForm1");
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
$knje364cCtl = new knje364cController;
?>
