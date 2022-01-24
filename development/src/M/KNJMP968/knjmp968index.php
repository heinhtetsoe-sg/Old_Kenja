<?php

require_once('for_php7.php');

require_once('knjmp968Model.inc');
require_once('knjmp968Query.inc');

class knjmp968Controller extends Controller {
    var $ModelClassName = "knjmp968Model";
    var $ProgramID      = "KNJMP968";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjmp968":                             //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjmp968Model();       //コントロールマスタの呼び出し
                    $this->callView("knjmp968Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjmp968Ctl = new knjmp968Controller;
?>
