<?php

require_once('for_php7.php');

require_once('knjg104aModel.inc');
require_once('knjg104aQuery.inc');

class knjg104aController extends Controller {
    var $ModelClassName = "knjg104aModel";
    var $ProgramID      = "KNJG104A";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjg104a":                             //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjg104aModel();       //コントロールマスタの呼び出し
                    $this->callView("knjg104aForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjg104aCtl = new knjg104aController;
?>
