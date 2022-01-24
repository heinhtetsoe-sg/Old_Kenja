<?php

require_once('for_php7.php');

require_once('knjh343Model.inc');
require_once('knjh343Query.inc');

class knjh343Controller extends Controller {
    var $ModelClassName = "knjh343Model";
    var $ProgramID      = "KNJH343";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjh343":                             //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjh343Model();       //コントロールマスタの呼び出し
                    $this->callView("knjh343Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }

    }
}
$knjh343Ctl = new knjh343Controller;
//var_dump($_REQUEST);
?>
