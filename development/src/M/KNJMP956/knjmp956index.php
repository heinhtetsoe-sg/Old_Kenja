<?php

require_once('for_php7.php');

require_once('knjmp956Model.inc');
require_once('knjmp956Query.inc');

class knjmp956Controller extends Controller {
    var $ModelClassName = "knjmp956Model";
    var $ProgramID      = "KNJMP956";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjmp956":                             //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjmp956Model();       //コントロールマスタの呼び出し
                    $this->callView("knjmp956Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjmp956Ctl = new knjmp956Controller;
?>
