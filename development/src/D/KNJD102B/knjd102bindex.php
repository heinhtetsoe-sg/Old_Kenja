<?php

require_once('for_php7.php');

require_once('knjd102bModel.inc');
require_once('knjd102bQuery.inc');

class knjd102bController extends Controller {
    var $ModelClassName = "knjd102bModel";
    var $ProgramID      = "KNJD102B";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "gakki":
                case "knjd102b":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjd102bModel();        //コントロールマスタの呼び出し
                    $this->callView("knjd102bForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjd102bForm1");
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
$knjd102bCtl = new knjd102bController;
//var_dump($_REQUEST);
?>
