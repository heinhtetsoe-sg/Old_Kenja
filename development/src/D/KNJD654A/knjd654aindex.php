<?php

require_once('for_php7.php');

require_once('knjd654aModel.inc');
require_once('knjd654aQuery.inc');

class knjd654aController extends Controller {
    var $ModelClassName = "knjd654aModel";
    var $ProgramID      = "KNJD654A";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd654a":                         //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjd654aModel();   //コントロールマスタの呼び出し
                    $this->callView("knjd654aForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjd654aForm1");
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
$knjd654aCtl = new knjd654aController;
//var_dump($_REQUEST);
?>
