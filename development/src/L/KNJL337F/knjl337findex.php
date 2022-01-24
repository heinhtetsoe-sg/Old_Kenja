<?php

require_once('for_php7.php');

require_once('knjl337fModel.inc');
require_once('knjl337fQuery.inc');

class knjl337fController extends Controller {
    var $ModelClassName = "knjl337fModel";
    var $ProgramID      = "KNJL337F";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl337f":						    //メニュー画面もしくはSUBMITした場合
					$sessionInstance->knjl337fModel();		//コントロールマスタの呼び出し
                    $this->callView("knjl337fForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
			}
        }
    }
}
$knjl337fCtl = new knjl337fController;
?>
