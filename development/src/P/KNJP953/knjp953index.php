<?php

require_once('for_php7.php');

require_once('knjp953Model.inc');
require_once('knjp953Query.inc');

class knjp953Controller extends Controller {
    var $ModelClassName = "knjp953Model";
    var $ProgramID      = "KNJP953";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjp953":                             //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjp953Model();       //コントロールマスタの呼び出し
                    $this->callView("knjp953Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjp953Ctl = new knjp953Controller;
?>
