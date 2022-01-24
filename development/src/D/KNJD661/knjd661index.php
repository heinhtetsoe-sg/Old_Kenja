<?php

require_once('for_php7.php');

require_once('knjd661Model.inc');
require_once('knjd661Query.inc');

class knjd661Controller extends Controller {
    var $ModelClassName = "knjd661Model";
    var $ProgramID      = "KNJD661";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd661":                             //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjd661Model();       //コントロールマスタの呼び出し
                    $this->callView("knjd661Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd661Ctl = new knjd661Controller;
//var_dump($_REQUEST);
?>
