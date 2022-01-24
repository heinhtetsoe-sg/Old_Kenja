<?php

require_once('for_php7.php');

require_once('knjd176cModel.inc');
require_once('knjd176cQuery.inc');

class knjd176cController extends Controller {
    var $ModelClassName = "knjd176cModel";
    var $ProgramID      = "KNJD176C";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "gakki":
                case "knjd176c":                         //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjd176cModel();   //コントロールマスタの呼び出し
                    $this->callView("knjd176cForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjd176cForm1");
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
$knjd176cCtl = new knjd176cController;
//var_dump($_REQUEST);
?>
