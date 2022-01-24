<?php

require_once('for_php7.php');

require_once('knjl323fModel.inc');
require_once('knjl323fQuery.inc');

class knjl323fController extends Controller {
    var $ModelClassName = "knjl323fModel";
    var $ProgramID      = "KNJL323F";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl323f":						    //メニュー画面もしくはSUBMITした場合
					$sessionInstance->knjl323fModel();		//コントロールマスタの呼び出し
                    $this->callView("knjl323fForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
			}
        }
    }
}
$knjl323fCtl = new knjl323fController;
?>
