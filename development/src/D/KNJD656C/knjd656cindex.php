<?php

require_once('for_php7.php');

require_once('knjd656cModel.inc');
require_once('knjd656cQuery.inc');

class knjd656cController extends Controller {
    var $ModelClassName = "knjd656cModel";
    var $ProgramID      = "KNJD656C";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd656c":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjd656cModel();        //コントロールマスタの呼び出し
                    $this->callView("knjd656cForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjd656cForm1");
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
$knjd656cCtl = new knjd656cController;
//var_dump($_REQUEST);
?>
