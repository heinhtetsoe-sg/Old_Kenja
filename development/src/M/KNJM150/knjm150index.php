<?php

require_once('for_php7.php');

require_once('knjm150Model.inc');
require_once('knjm150Query.inc');

class knjm150Controller extends Controller {
    var $ModelClassName = "knjm150Model";
    var $ProgramID      = "KNJM150";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjm150":                             //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjm150Model();       //コントロールマスタの呼び出し
                    $this->callView("knjm150Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjm150Ctl = new knjm150Controller;
//var_dump($_REQUEST);
?>
