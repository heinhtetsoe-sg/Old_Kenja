<?php

require_once('for_php7.php');

require_once('knjd171fModel.inc');
require_once('knjd171fQuery.inc');

class knjd171fController extends Controller {
    var $ModelClassName = "knjd171fModel";
    var $ProgramID      = "KNJD171F";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd171f":                            //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjd171fModel();      //コントロールマスタの呼び出し
                    $this->callView("knjd171fForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd171fCtl = new knjd171fController;
?>
