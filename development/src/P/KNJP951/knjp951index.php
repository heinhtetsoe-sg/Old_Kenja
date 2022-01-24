<?php

require_once('for_php7.php');

require_once('knjp951Model.inc');
require_once('knjp951Query.inc');

class knjp951Controller extends Controller {
    var $ModelClassName = "knjp951Model";
    var $ProgramID      = "KNJP951";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjp951":                             //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjp951Model();       //コントロールマスタの呼び出し
                    $this->callView("knjp951Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjp951Ctl = new knjp951Controller;
?>
