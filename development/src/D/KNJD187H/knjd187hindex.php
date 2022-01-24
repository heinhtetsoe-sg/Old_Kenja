<?php

require_once('for_php7.php');

require_once('knjd187hModel.inc');
require_once('knjd187hQuery.inc');

class knjd187hController extends Controller {
    var $ModelClassName = "knjd187hModel";
    var $ProgramID      = "KNJD187H";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "gakki":
                case "knjd187h":                             //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjd187hModel();       //コントロールマスタの呼び出し
                    $this->callView("knjd187hForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjd187hForm1");
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
$knjd187hCtl = new knjd187hController;
//var_dump($_REQUEST);
?>
