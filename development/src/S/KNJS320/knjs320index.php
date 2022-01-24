<?php

require_once('for_php7.php');

require_once('knjs320Model.inc');
require_once('knjs320Query.inc');

class knjs320Controller extends Controller {
    var $ModelClassName = "knjs320Model";
    var $ProgramID      = "KNJS320";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjs320":                              //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjs320Model();        //コントロールマスタの呼び出し
                    $this->callView("knjs320Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjs320Ctl = new knjs320Controller;
?>
