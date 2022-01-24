<?php

require_once('for_php7.php');

require_once('knjmp967Model.inc');
require_once('knjmp967Query.inc');

class knjmp967Controller extends Controller {
    var $ModelClassName = "knjmp967Model";
    var $ProgramID      = "KNJMP967";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjmp967":                             //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjmp967Model();       //コントロールマスタの呼び出し
                    $this->callView("knjmp967Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjmp967Ctl = new knjmp967Controller;
?>
