<?php

require_once('for_php7.php');

require_once('knjmp951Model.inc');
require_once('knjmp951Query.inc');

class knjmp951Controller extends Controller {
    var $ModelClassName = "knjmp951Model";
    var $ProgramID      = "KNJMP951";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjmp951":                             //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjmp951Model();       //コントロールマスタの呼び出し
                    $this->callView("knjmp951Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjmp951Ctl = new knjmp951Controller;
?>
