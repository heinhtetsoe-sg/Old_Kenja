<?php

require_once('for_php7.php');

require_once('knjd184oModel.inc');
require_once('knjd184oQuery.inc');

class knjd184oController extends Controller {
    var $ModelClassName = "knjd184oModel";
    var $ProgramID      = "KNJD184O";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "gakki":
                case "knjd184o":                             //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjd184oModel();       //コントロールマスタの呼び出し
                    $this->callView("knjd184oForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjd184oForm1");
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
$knjd184oCtl = new knjd184oController;
//var_dump($_REQUEST);
?>
