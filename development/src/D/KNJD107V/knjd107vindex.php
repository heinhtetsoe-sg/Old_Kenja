<?php

require_once('for_php7.php');

require_once('knjd107vModel.inc');
require_once('knjd107vQuery.inc');

class knjd107vController extends Controller {
    var $ModelClassName = "knjd107vModel";
    var $ProgramID      = "KNJD107V";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "gakki":
                case "knjd107v":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjd107vModel();        //コントロールマスタの呼び出し
                    $this->callView("knjd107vForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjd107vForm1");
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
$knjd107vCtl = new knjd107vController;
//var_dump($_REQUEST);
?>
