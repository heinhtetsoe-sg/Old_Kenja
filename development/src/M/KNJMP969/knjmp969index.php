<?php

require_once('for_php7.php');

require_once('knjmp969Model.inc');
require_once('knjmp969Query.inc');

class knjmp969Controller extends Controller {
    var $ModelClassName = "knjmp969Model";
    var $ProgramID      = "KNJMP969";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjmp969":                            //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjmp969Model();      //コントロールマスタの呼び出し
                    $this->callView("knjmp969Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjmp969Ctl = new knjmp969Controller;
?>
