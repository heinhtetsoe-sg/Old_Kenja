<?php

require_once('for_php7.php');

require_once('knjmp962Model.inc');
require_once('knjmp962Query.inc');

class knjmp962Controller extends Controller {
    var $ModelClassName = "knjmp962Model";
    var $ProgramID      = "KNJMP962";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjmp962":                       //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjmp962Model(); //コントロールマスタの呼び出し
                    $this->callView("knjmp962Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjmp962Ctl = new knjmp962Controller;
//var_dump($_REQUEST);
?>
