<?php

require_once('for_php7.php');

require_once('knjf034Model.inc');
require_once('knjf034Query.inc');

class knjf034Controller extends Controller {
    var $ModelClassName = "knjf034Model";
    var $ProgramID      = "KNJF034";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjf034":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjf034Model();      //コントロールマスタの呼び出し
                    $this->callView("knjf034Form1");
                    exit;
                case "clickchange":                         //メニュー画面もしくはSUBMITした場合 //NO001
                    $sessionInstance->knjf034Model();      //コントロールマスタの呼び出し
                    $this->callView("knjf034Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knjf034Ctl = new knjf034Controller;
//var_dump($_REQUEST);
?>
