<?php

require_once('for_php7.php');

require_once('knjd171gModel.inc');
require_once('knjd171gQuery.inc');

class knjd171gController extends Controller {
    var $ModelClassName = "knjd171gModel";
    var $ProgramID      = "KNJD171G";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd171g":                            //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjd171gModel();      //コントロールマスタの呼び出し
                    $this->callView("knjd171gForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd171gCtl = new knjd171gController;
?>
