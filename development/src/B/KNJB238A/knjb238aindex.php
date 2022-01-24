<?php

require_once('for_php7.php');

require_once('knjb238aModel.inc');
require_once('knjb238aQuery.inc');

class knjb238aController extends Controller {
    var $ModelClassName = "knjb238aModel";
    var $ProgramID      = "KNJB238A";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjb238a":                             //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjb238aModel();       //コントロールマスタの呼び出し
                    $this->callView("knjb238aForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjb238aCtl = new knjb238aController;
?>
