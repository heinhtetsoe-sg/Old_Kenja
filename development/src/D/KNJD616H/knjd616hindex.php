<?php

require_once('for_php7.php');

require_once('knjd616hModel.inc');
require_once('knjd616hQuery.inc');

class knjd616hController extends Controller {
    var $ModelClassName = "knjd616hModel";
    var $ProgramID      = "KNJD616H";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd616h":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjd616hModel();      //コントロールマスタの呼び出し
                    $this->callView("knjd616hForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd616hCtl = new knjd616hController;
?>
