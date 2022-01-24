<?php

require_once('for_php7.php');

require_once('knjmp964Model.inc');
require_once('knjmp964Query.inc');

class knjmp964Controller extends Controller {
    var $ModelClassName = "knjmp964Model";
    var $ProgramID      = "KNJMP964";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjmp964":                       //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjmp964Model(); //コントロールマスタの呼び出し
                    $this->callView("knjmp964Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjmp964Ctl = new knjmp964Controller;
//var_dump($_REQUEST);
?>
