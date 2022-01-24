<?php

require_once('for_php7.php');

require_once('knjl334fModel.inc');
require_once('knjl334fQuery.inc');

class knjl334fController extends Controller {
    var $ModelClassName = "knjl334fModel";
    var $ProgramID      = "KNJL334F";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl334f":						    //メニュー画面もしくはSUBMITした場合
					$sessionInstance->knjl334fModel();		//コントロールマスタの呼び出し
                    $this->callView("knjl334fForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
			}
        }
    }
}
$knjl334fCtl = new knjl334fController;
?>
