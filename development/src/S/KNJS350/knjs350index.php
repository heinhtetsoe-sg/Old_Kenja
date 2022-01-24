<?php

require_once('for_php7.php');

require_once('knjs350Model.inc');
require_once('knjs350Query.inc');

class knjs350Controller extends Controller {
    var $ModelClassName = "knjs350Model";
    var $ProgramID      = "KNJS350";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "main";
                    $this->callView("knjs350Form1");
                    break 2;
                case "knjs350":                              //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjs350Model();        //コントロールマスタの呼び出し
                    $this->callView("knjs350Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjs350Ctl = new knjs350Controller;
?>
