<?php

require_once('for_php7.php');

require_once('knjh561aModel.inc');
require_once('knjh561aQuery.inc');

class knjh561aController extends Controller {
    var $ModelClassName = "knjh561aModel";
    var $ProgramID      = "KNJH561A";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "csv":
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjh561aForm1");
                    }
                    break 2;
                case "":
                case "knjh561a":                             //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjh561aModel();       //コントロールマスタの呼び出し
                    $this->callView("knjh561aForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjh561aCtl = new knjh561aController;
?>
