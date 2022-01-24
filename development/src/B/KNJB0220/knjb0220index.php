<?php

require_once('for_php7.php');

require_once('knjb0220Model.inc');
require_once('knjb0220Query.inc');

class knjb0220Controller extends Controller {
    var $ModelClassName = "knjb0220Model";
    var $ProgramID      = "KNJB0220";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjb0220":                         //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjb0220Model();   //コントロールマスタの呼び出し
                    $this->callView("knjb0220Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjb0220Ctl = new knjb0220Controller;
?>
