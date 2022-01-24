<?php

require_once('for_php7.php');

require_once('knjd184nModel.inc');
require_once('knjd184nQuery.inc');

class knjd184nController extends Controller {
    var $ModelClassName = "knjd184nModel";
    var $ProgramID      = "KNJD184N";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd184n":                            //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjd184nModel();      //コントロールマスタの呼び出し
                    $this->callView("knjd184nForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd184nCtl = new knjd184nController;
?>
