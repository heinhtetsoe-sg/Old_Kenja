<?php

require_once('for_php7.php');

require_once('knjs331Model.inc');
require_once('knjs331Query.inc');

class knjs331Controller extends Controller {
    var $ModelClassName = "knjs331Model";
    var $ProgramID      = "KNJS331";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjs331":                              //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjs331Model();        //コントロールマスタの呼び出し
                    $this->callView("knjs331Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjs331Ctl = new knjs331Controller;
?>
