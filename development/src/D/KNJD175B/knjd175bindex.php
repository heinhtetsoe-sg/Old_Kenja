<?php

require_once('for_php7.php');

require_once('knjd175bModel.inc');
require_once('knjd175bQuery.inc');

class knjd175bController extends Controller {
    var $ModelClassName = "knjd175bModel";
    var $ProgramID      = "KNJD175B";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "gakki":
                case "knjd175b":                             //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjd175bModel();       //コントロールマスタの呼び出し
                    $this->callView("knjd175bForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjd175bForm1");
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
$knjd175bCtl = new knjd175bController;
//var_dump($_REQUEST);
?>
