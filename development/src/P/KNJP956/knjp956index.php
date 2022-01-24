<?php

require_once('for_php7.php');

require_once('knjp956Model.inc');
require_once('knjp956Query.inc');

class knjp956Controller extends Controller {
    var $ModelClassName = "knjp956Model";
    var $ProgramID      = "KNJP956";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjp956":                             //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjp956Model();       //コントロールマスタの呼び出し
                    $this->callView("knjp956Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjp956Ctl = new knjp956Controller;
?>
