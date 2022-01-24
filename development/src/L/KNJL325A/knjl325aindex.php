<?php

require_once('for_php7.php');

require_once('knjl325aModel.inc');
require_once('knjl325aQuery.inc');

class knjl325aController extends Controller {
    var $ModelClassName = "knjl325aModel";
    var $ProgramID      = "KNJL325A";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl325a":						    //メニュー画面もしくはSUBMITした場合
					$sessionInstance->knjl325aModel();		//コントロールマスタの呼び出し
                    $this->callView("knjl325aForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
			}
        }
    }
}
$knjl325aCtl = new knjl325aController;
?>
