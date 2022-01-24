<?php

require_once('for_php7.php');

require_once('knjh172Model.inc');
require_once('knjh172Query.inc');

class knjh172Controller extends Controller {
    var $ModelClassName = "knjh172Model";
    var $ProgramID      = "KNJH172";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjh172":                        //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjh172Model();  //コントロールマスタの呼び出し
                    $this->callView("knjh172Form1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjh172Form1");
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
$knjh172Ctl = new knjh172Controller;
//var_dump($_REQUEST);
?>
