<?php

require_once('for_php7.php');

require_once('knjd184mModel.inc');
require_once('knjd184mQuery.inc');

class knjd184mController extends Controller {
    var $ModelClassName = "knjd184mModel";
    var $ProgramID      = "KNJD184M";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd184m":                            //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjd184mModel();      //コントロールマスタの呼び出し
                    $this->callView("knjd184mForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd184mCtl = new knjd184mController;
?>
