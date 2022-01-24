<?php

require_once('for_php7.php');

require_once('knjd656Model.inc');
require_once('knjd656Query.inc');

class knjd656Controller extends Controller {
    var $ModelClassName = "knjd656Model";
    var $ProgramID      = "KNJD656";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd656":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjd656Model();        //コントロールマスタの呼び出し
                    $this->callView("knjd656Form1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjd656Form1");
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
$knjd656Ctl = new knjd656Controller;
//var_dump($_REQUEST);
?>
