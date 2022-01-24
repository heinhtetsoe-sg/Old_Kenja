<?php

require_once('for_php7.php');

require_once('knjd171Model.inc');
require_once('knjd171Query.inc');

class knjd171Controller extends Controller {
    var $ModelClassName = "knjd171Model";
    var $ProgramID      = "KNJD171";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "gakki":
                case "knjd171":                             //メニュー画面もしくはSUBMITした場合
                case "clickcheng":                          //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjd171Model();       //コントロールマスタの呼び出し
                    $this->callView("knjd171Form1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjd171Form1");
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
$knjd171Ctl = new knjd171Controller;
//var_dump($_REQUEST);
?>
