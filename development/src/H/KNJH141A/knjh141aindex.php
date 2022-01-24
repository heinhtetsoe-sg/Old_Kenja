<?php

require_once('for_php7.php');

require_once('knjh141aModel.inc');
require_once('knjh141aQuery.inc');

class knjh141aController extends Controller {
    var $ModelClassName = "knjh141aModel";
    var $ProgramID      = "KNJH141A";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjh141a":                            //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjh141aModel();      //コントロールマスタの呼び出し
                    $this->callView("knjh141aForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjh141aForm1");
                    }
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjh141aCtl = new knjh141aController;
?>
