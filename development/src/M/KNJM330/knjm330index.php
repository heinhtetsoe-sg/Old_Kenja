<?php

require_once('for_php7.php');

require_once('knjm330Model.inc');
require_once('knjm330Query.inc');

class knjm330Controller extends Controller {
    var $ModelClassName = "knjm330Model";
    var $ProgramID      = "knjm330";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjm330":                             //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjm330Model();       //コントロールマスタの呼び出し
                    $this->callView("knjm330Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knjm330Ctl = new knjm330Controller;
//var_dump($_REQUEST);
?>
