<?php

require_once('for_php7.php');

require_once('knjmp971Model.inc');
require_once('knjmp971Query.inc');

class knjmp971Controller extends Controller {
    var $ModelClassName = "knjmp971Model";
    var $ProgramID      = "KNJMP971";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjmp971":                            //メニュー画面もしくはSUBMITした場合
                case "read":
                    $sessionInstance->knjmp971Model();      //コントロールマスタの呼び出し
                    $this->callView("knjmp971Form1");
                    exit;
                case "update":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("read");
                    break 1;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjmp971Ctl = new knjmp971Controller;
?>
