<?php

require_once('for_php7.php');

require_once('knja227aModel.inc');
require_once('knja227aQuery.inc');

class knja227aController extends Controller {
    var $ModelClassName = "knja227aModel";
    var $ProgramID      = "KNJA227A";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knja227a":								//メニュー画面もしくはSUBMITした場合
					$sessionInstance->knja227aModel();		//コントロールマスタの呼び出し
                    $this->callView("knja227aForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
			}
        }
    }
}
$knja227aCtl = new knja227aController;
?>
