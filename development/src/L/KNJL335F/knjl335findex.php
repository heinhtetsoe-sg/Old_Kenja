<?php

require_once('for_php7.php');

require_once('knjl335fModel.inc');
require_once('knjl335fQuery.inc');

class knjl335fController extends Controller {
    var $ModelClassName = "knjl335fModel";
    var $ProgramID      = "KNJL335F";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl335f":						    //メニュー画面もしくはSUBMITした場合
					$sessionInstance->knjl335fModel();		//コントロールマスタの呼び出し
                    $this->callView("knjl335fForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
			}
        }
    }
}
$knjl335fCtl = new knjl335fController;
?>
