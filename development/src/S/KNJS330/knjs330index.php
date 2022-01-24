<?php

require_once('for_php7.php');

require_once('knjs330Model.inc');
require_once('knjs330Query.inc');

class knjs330Controller extends Controller {
    var $ModelClassName = "knjs330Model";
    var $ProgramID      = "KNJS330";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjs330":                              //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjs330Model();        //コントロールマスタの呼び出し
                    $this->callView("knjs330Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjs330Ctl = new knjs330Controller;
?>
