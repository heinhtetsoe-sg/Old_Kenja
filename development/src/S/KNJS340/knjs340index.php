<?php

require_once('for_php7.php');

require_once('knjs340Model.inc');
require_once('knjs340Query.inc');

class knjs340Controller extends Controller {
    var $ModelClassName = "knjs340Model";
    var $ProgramID      = "KNJS340";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "main";
                    $this->callView("knjs340Form1");
                    break 2;
                case "knjs340":                              //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjs340Model();        //コントロールマスタの呼び出し
                    $this->callView("knjs340Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjs340Ctl = new knjs340Controller;
?>
