<?php

require_once('for_php7.php');

require_once('knjl333fModel.inc');
require_once('knjl333fQuery.inc');

class knjl333fController extends Controller {
    var $ModelClassName = "knjl333fModel";
    var $ProgramID      = "KNJL333F";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl333f":						    //メニュー画面もしくはSUBMITした場合
					$sessionInstance->knjl333fModel();		//コントロールマスタの呼び出し
                    $this->callView("knjl333fForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
			}
        }
    }
}
$knjl333fCtl = new knjl333fController;
?>
