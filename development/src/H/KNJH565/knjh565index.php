<?php

require_once('for_php7.php');

require_once('knjh565Model.inc');
require_once('knjh565Query.inc');

class knjh565Controller extends Controller {
    var $ModelClassName = "knjh565Model";
    var $ProgramID      = "KNJH565";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjh565":                                //メニュー画面もしくはSUBMITした場合
                case "edit":                                //メニュー画面もしくはSUBMITした場合
                case "change_grade":
                    $sessionInstance->knjh565Model();        //コントロールマスタの呼び出し
                    $this->callView("knjh565Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjh565Ctl = new knjh565Controller;
//var_dump($_REQUEST);
?>
