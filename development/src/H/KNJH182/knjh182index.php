<?php

require_once('for_php7.php');

require_once('knjh182Model.inc');
require_once('knjh182Query.inc');

class knjh182Controller extends Controller {
    var $ModelClassName = "knjh182Model";
    var $ProgramID      = "KNJH182";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "csv":
                case "knjh182":                        //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjh182Model();  //コントロールマスタの呼び出し
                    $this->callView("knjh182Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjh182Ctl = new knjh182Controller;
//var_dump($_REQUEST);
?>
