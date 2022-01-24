<?php

require_once('for_php7.php');

require_once('knjp954Model.inc');
require_once('knjp954Query.inc');

class knjp954Controller extends Controller {
    var $ModelClassName = "knjp954Model";
    var $ProgramID      = "KNJP954";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjp954":                             //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjp954Model();       //コントロールマスタの呼び出し
                    $this->callView("knjp954Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjp954Ctl = new knjp954Controller;
?>
