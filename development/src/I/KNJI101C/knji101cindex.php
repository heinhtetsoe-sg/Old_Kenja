<?php

require_once('for_php7.php');

require_once('knji101cModel.inc');
require_once('knji101cQuery.inc');

class knji101cController extends Controller {
    var $ModelClassName = "knji101cModel";
    var $ProgramID      = "KNJI101C";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knji101c":				        //メニュー画面もしくはSUBMITした場合
					$sessionInstance->knji101cModel();	//コントロールマスタの呼び出し
                    $this->callView("knji101cForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
			}
        }
    }
}
$knji101cCtl = new knji101cController;
?>
