<?php

require_once('for_php7.php');

require_once('knjg047Model.inc');
require_once('knjg047Query.inc');

class knjg047Controller extends Controller {
    var $ModelClassName = "knjg047Model";
    var $ProgramID      = "KNJG047";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjg047":                             //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjg047Model();       //コントロールマスタの呼び出し
                    $this->callView("knjg047Form1");
                    exit;
                case "csv":     //CSVダウンロード
                    $sessionInstance->setAccessLogDetail("EO", $ProgramID);
                    if (!$sessionInstance->getDownloadModel()) {
                        $this->callView("knjg047Form1");
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
$knjg047Ctl = new knjg047Controller;
?>
