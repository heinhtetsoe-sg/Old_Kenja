<?php

require_once('for_php7.php');

require_once('knjd176bModel.inc');
require_once('knjd176bQuery.inc');

class knjd176bController extends Controller {
    var $ModelClassName = "knjd176bModel";
    var $ProgramID      = "KNJD176B";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "gakki":
                case "knjd176b":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjd176bModel();      //コントロールマスタの呼び出し
                    $this->callView("knjd176bForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjd176bForm1");
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
$knjd176bCtl = new knjd176bController;
//var_dump($_REQUEST);
?>
