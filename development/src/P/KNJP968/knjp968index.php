<?php

require_once('for_php7.php');

require_once('knjp968Model.inc');
require_once('knjp968Query.inc');

class knjp968Controller extends Controller {
    var $ModelClassName = "knjp968Model";
    var $ProgramID      = "KNJP968";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjp968":                             //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjp968Model();       //コントロールマスタの呼び出し
                    $this->callView("knjp968Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjp968Ctl = new knjp968Controller;
?>
