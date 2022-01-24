<?php

require_once('for_php7.php');

require_once('knjmp955Model.inc');
require_once('knjmp955Query.inc');

class knjmp955Controller extends Controller {
    var $ModelClassName = "knjmp955Model";
    var $ProgramID      = "KNJMP955";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjmp955":                             //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjmp955Model();       //コントロールマスタの呼び出し
                    $this->callView("knjmp955Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjmp955Ctl = new knjmp955Controller;
?>
