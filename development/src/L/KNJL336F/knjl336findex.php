<?php

require_once('for_php7.php');

require_once('knjl336fModel.inc');
require_once('knjl336fQuery.inc');

class knjl336fController extends Controller {
    var $ModelClassName = "knjl336fModel";
    var $ProgramID      = "KNJL336F";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl336f":						    //メニュー画面もしくはSUBMITした場合
					$sessionInstance->knjl336fModel();		//コントロールマスタの呼び出し
                    $this->callView("knjl336fForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
			}
        }
    }
}
$knjl336fCtl = new knjl336fController;
?>
