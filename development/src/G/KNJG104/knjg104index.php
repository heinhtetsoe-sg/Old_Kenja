<?php

require_once('for_php7.php');

require_once('knjg104Model.inc');
require_once('knjg104Query.inc');

class knjg104Controller extends Controller {
    var $ModelClassName = "knjg104Model";
    var $ProgramID      = "KNJG104";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjg104":                             //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjg104Model();       //コントロールマスタの呼び出し
                    $this->callView("knjg104Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjg104Ctl = new knjg104Controller;
?>
