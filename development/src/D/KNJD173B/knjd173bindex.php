<?php

require_once('for_php7.php');

require_once('knjd173bModel.inc');
require_once('knjd173bQuery.inc');

class knjd173bController extends Controller {
    var $ModelClassName = "knjd173bModel";
    var $ProgramID      = "KNJD173B";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "gakki":
                case "knjd173b":                            //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjd173bModel();      //コントロールマスタの呼び出し
                    $this->callView("knjd173bForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd173bCtl = new knjd173bController;
//var_dump($_REQUEST);
?>
