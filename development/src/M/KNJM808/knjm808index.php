<?php

require_once('for_php7.php');

require_once('knjm808Model.inc');
require_once('knjm808Query.inc');

class knjm808Controller extends Controller {
    var $ModelClassName = "knjm808Model";
    var $ProgramID      = "KNJM808";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjm808":                         //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjm808Model();   //コントロールマスタの呼び出し
                    $this->callView("knjm808Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjm808Ctl = new knjm808Controller;
?>
