<?php

require_once('for_php7.php');

require_once('knjd175Model.inc');
require_once('knjd175Query.inc');

class knjd175Controller extends Controller {
    var $ModelClassName = "knjd175Model";
    var $ProgramID      = "KNJD175";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "gakki":
                case "knjd175":                             //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjd175Model();       //コントロールマスタの呼び出し
                    $this->callView("knjd175Form1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjd175Form1");
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
$knjd175Ctl = new knjd175Controller;
//var_dump($_REQUEST);
?>
