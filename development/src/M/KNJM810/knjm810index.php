<?php

require_once('for_php7.php');

require_once('knjm810Model.inc');
require_once('knjm810Query.inc');

class knjm810Controller extends Controller {
    var $ModelClassName = "knjm810Model";
    var $ProgramID      = "KNJM810";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjm810":                         //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjm810Model();   //コントロールマスタの呼び出し
                    $this->callView("knjm810Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjm810Ctl = new knjm810Controller;
?>
