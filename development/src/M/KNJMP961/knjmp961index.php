<?php

require_once('for_php7.php');

require_once('knjmp961Model.inc');
require_once('knjmp961Query.inc');

class knjmp961Controller extends Controller {
    var $ModelClassName = "knjmp961Model";
    var $ProgramID      = "KNJMP961";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjmp961":                             //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjmp961Model();       //コントロールマスタの呼び出し
                    $this->callView("knjmp961Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjmp961Ctl = new knjmp961Controller;
?>
