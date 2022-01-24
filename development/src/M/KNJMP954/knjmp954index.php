<?php

require_once('for_php7.php');

require_once('knjmp954Model.inc');
require_once('knjmp954Query.inc');

class knjmp954Controller extends Controller {
    var $ModelClassName = "knjmp954Model";
    var $ProgramID      = "KNJMP954";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjmp954":                             //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjmp954Model();       //コントロールマスタの呼び出し
                    $this->callView("knjmp954Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjmp954Ctl = new knjmp954Controller;
?>
