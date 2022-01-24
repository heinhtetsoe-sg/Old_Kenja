<?php

require_once('for_php7.php');

require_once('knjd102sModel.inc');
require_once('knjd102sQuery.inc');

class knjd102sController extends Controller {
    var $ModelClassName = "knjd102sModel";
    var $ProgramID      = "KNJD102S";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "gakki":
                case "knjd102s":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjd102sModel();        //コントロールマスタの呼び出し
                    $this->callView("knjd102sForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjd102sForm1");
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
$knjd102sCtl = new knjd102sController;
//var_dump($_REQUEST);
?>
