<?php

require_once('for_php7.php');

require_once('knjd175aModel.inc');
require_once('knjd175aQuery.inc');

class knjd175aController extends Controller {
    var $ModelClassName = "knjd175aModel";
    var $ProgramID      = "KNJD175A";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "gakki":
                case "knjd175a":                             //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjd175aModel();       //コントロールマスタの呼び出し
                    $this->callView("knjd175aForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjd175aForm1");
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
$knjd175aCtl = new knjd175aController;
//var_dump($_REQUEST);
?>
