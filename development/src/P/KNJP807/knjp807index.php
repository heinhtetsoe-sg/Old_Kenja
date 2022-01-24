<?php

require_once('for_php7.php');

require_once('knjp807Model.inc');
require_once('knjp807Query.inc');

class knjp807Controller extends Controller {
    var $ModelClassName = "knjp807Model";
    var $ProgramID      = "KNJP807";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "main":
                case "knjp807":                              //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjp807Model();        //コントロールマスタの呼び出し
                    $this->callView("knjp807Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjp807Ctl = new knjp807Controller;
//var_dump($_REQUEST);
?>
