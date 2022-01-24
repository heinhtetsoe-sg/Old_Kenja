<?php

require_once('for_php7.php');

require_once('knjd654lModel.inc');
require_once('knjd654lQuery.inc');

class knjd654lController extends Controller {
    var $ModelClassName = "knjd654lModel";
    var $ProgramID      = "KNJD654L";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd654l":                         //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjd654lModel();   //コントロールマスタの呼び出し
                    $this->callView("knjd654lForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjd654lForm1");
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
$knjd654lCtl = new knjd654lController;
//var_dump($_REQUEST);
?>
