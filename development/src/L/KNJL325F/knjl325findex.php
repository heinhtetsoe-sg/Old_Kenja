<?php

require_once('for_php7.php');

require_once('knjl325fModel.inc');
require_once('knjl325fQuery.inc');

class knjl325fController extends Controller {
    var $ModelClassName = "knjl325fModel";
    var $ProgramID      = "KNJL325F";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl325f":						    //メニュー画面もしくはSUBMITした場合
					$sessionInstance->knjl325fModel();		//コントロールマスタの呼び出し
                    $this->callView("knjl325fForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
			}
        }
    }
}
$knjl325fCtl = new knjl325fController;
?>
