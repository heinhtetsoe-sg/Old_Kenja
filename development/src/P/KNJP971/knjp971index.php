<?php

require_once('for_php7.php');

require_once('knjp971Model.inc');
require_once('knjp971Query.inc');

class knjp971Controller extends Controller {
    var $ModelClassName = "knjp971Model";
    var $ProgramID      = "KNJP971";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjp971":                            //メニュー画面もしくはSUBMITした場合
                case "read":
                    $sessionInstance->knjp971Model();      //コントロールマスタの呼び出し
                    $this->callView("knjp971Form1");
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
$knjp971Ctl = new knjp971Controller;
?>
