<?php

require_once('for_php7.php');

require_once('knjd175cModel.inc');
require_once('knjd175cQuery.inc');

class knjd175cController extends Controller {
    var $ModelClassName = "knjd175cModel";
    var $ProgramID      = "KNJD175C";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "gakki":
                case "knjd175c":                             //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjd175cModel();       //コントロールマスタの呼び出し
                    $this->callView("knjd175cForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjd175cForm1");
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
$knjd175cCtl = new knjd175cController;
//var_dump($_REQUEST);
?>
