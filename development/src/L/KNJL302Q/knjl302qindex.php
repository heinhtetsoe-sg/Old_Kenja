<?php

require_once('for_php7.php');

require_once('knjl302qModel.inc');
require_once('knjl302qQuery.inc');

class knjl302qController extends Controller {
    var $ModelClassName = "knjl302qModel";
    var $ProgramID      = "KNJL302Q";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl302q":                            //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjl302qModel();      //コントロールマスタの呼び出し
                    $this->callView("knjl302qForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl302qCtl = new knjl302qController;
?>
