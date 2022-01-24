<?php

require_once('for_php7.php');

require_once('knjd657bModel.inc');
require_once('knjd657bQuery.inc');

class knjd657bController extends Controller {
    var $ModelClassName = "knjd657bModel";
    var $ProgramID      = "KNJD657B";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd657b":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjd657bModel();        //コントロールマスタの呼び出し
                    $this->callView("knjd657bForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjd657bForm1");
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
$knjd657bCtl = new knjd657bController;
//var_dump($_REQUEST);
?>
