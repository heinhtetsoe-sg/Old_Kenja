<?php

require_once('for_php7.php');

require_once('knjl332fModel.inc');
require_once('knjl332fQuery.inc');

class knjl332fController extends Controller {
    var $ModelClassName = "knjl332fModel";
    var $ProgramID      = "KNJL332F";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl332f":						    //メニュー画面もしくはSUBMITした場合
					$sessionInstance->knjl332fModel();		//コントロールマスタの呼び出し
                    $this->callView("knjl332fForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
			}
        }
    }
}
$knjl332fCtl = new knjl332fController;
?>
