<?php

require_once('for_php7.php');

require_once('knjl312fModel.inc');
require_once('knjl312fQuery.inc');

class knjl312fController extends Controller {
    var $ModelClassName = "knjl312fModel";
    var $ProgramID      = "KNJL312F";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl312f":						    //メニュー画面もしくはSUBMITした場合
					$sessionInstance->knjl312fModel();		//コントロールマスタの呼び出し
                    $this->callView("knjl312fForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
			}
        }
    }
}
$knjl312fCtl = new knjl312fController;
?>
