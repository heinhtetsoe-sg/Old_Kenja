<?php

require_once('for_php7.php');

require_once('knja226Model.inc');
require_once('knja226Query.inc');

class knja226Controller extends Controller {
    var $ModelClassName = "knja226Model";
    var $ProgramID      = "KNJA226";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "main";
                    $this->callView("knja226Form1");
                    break 2;
                case "knja226":                              //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knja226Model();        //コントロールマスタの呼び出し
                    $this->callView("knja226Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knja226Ctl = new knja226Controller;
?>
