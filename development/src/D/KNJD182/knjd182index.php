<?php

require_once('for_php7.php');

require_once('knjd182Model.inc');
require_once('knjd182Query.inc');

class knjd182Controller extends Controller {
    var $ModelClassName = "knjd182Model";
    var $ProgramID      = "KNJD182";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "gakki":
                case "knjd182":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjd182Model();        //コントロールマスタの呼び出し
                    $this->callView("knjd182Form1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjd182Form1");
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
$knjd182Ctl = new knjd182Controller;
//var_dump($_REQUEST);
?>
