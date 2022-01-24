<?php

require_once('for_php7.php');

require_once('knjf030gModel.inc');
require_once('knjf030gQuery.inc');

class knjf030gController extends Controller {
    var $ModelClassName = "knjf030gModel";
    var $ProgramID        = "KNJF030G";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjf030g":                             //メニュー画面もしくはSUBMITした場合
                case "change_class":                        //クラス変更時のSUBMITした場合
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $sessionInstance->knjf030gModel();       //コントロールマスタの呼び出し
                    $this->callView("knjf030gForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjf030gCtl = new knjf030gController;
var_dump($_REQUEST);
?>
