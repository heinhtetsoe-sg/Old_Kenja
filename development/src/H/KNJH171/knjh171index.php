<?php

require_once('for_php7.php');

require_once('knjh171Model.inc');
require_once('knjh171Query.inc');

class knjh171Controller extends Controller {
    var $ModelClassName = "knjh171Model";
    var $ProgramID      = "KNJH171";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjh171":                        //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjh171Model();  //コントロールマスタの呼び出し
                    $this->callView("knjh171Form1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjh171Form1");
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
$knjh171Ctl = new knjh171Controller;
//var_dump($_REQUEST);
?>
