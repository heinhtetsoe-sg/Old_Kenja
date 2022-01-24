<?php

require_once('for_php7.php');

require_once('knjmp963Model.inc');
require_once('knjmp963Query.inc');

class knjmp963Controller extends Controller {
    var $ModelClassName = "knjmp963Model";
    var $ProgramID      = "KNJMP963";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjmp963":                             //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjmp963Model();       //コントロールマスタの呼び出し
                    $this->callView("knjmp963Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjmp963Ctl = new knjmp963Controller;
?>
