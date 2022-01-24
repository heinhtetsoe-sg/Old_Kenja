<?php

require_once('for_php7.php');

require_once('knjmp965Model.inc');
require_once('knjmp965Query.inc');

class knjmp965Controller extends Controller {
    var $ModelClassName = "knjmp965Model";
    var $ProgramID      = "KNJMP965";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjmp965":                             //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjmp965Model();       //コントロールマスタの呼び出し
                    $this->callView("knjmp965Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjmp965Ctl = new knjmp965Controller;
?>
