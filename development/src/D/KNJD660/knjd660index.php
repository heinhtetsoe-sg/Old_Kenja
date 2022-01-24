<?php

require_once('for_php7.php');

require_once('knjd660Model.inc');
require_once('knjd660Query.inc');

class knjd660Controller extends Controller {
    var $ModelClassName = "knjd660Model";
    var $ProgramID      = "KNJD660";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd660":                             //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjd660Model();       //コントロールマスタの呼び出し
                    $this->callView("knjd660Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd660Ctl = new knjd660Controller;
//var_dump($_REQUEST);
?>
