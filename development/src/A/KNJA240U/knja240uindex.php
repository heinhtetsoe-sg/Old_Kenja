<?php

require_once('for_php7.php');

require_once('knja240uModel.inc');
require_once('knja240uQuery.inc');

class knja240uController extends Controller {
    var $ModelClassName = "knja240uModel";
    var $ProgramID      = "KNJA240U";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knja240u":								//メニュー画面もしくはSUBMITした場合
					$sessionInstance->knja240uModel();		//コントロールマスタの呼び出し
                    $this->callView("knja240uForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
			}
        }
    }
}
$knja240uCtl = new knja240uController;
?>
