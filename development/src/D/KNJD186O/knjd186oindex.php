<?php

require_once('for_php7.php');

require_once('knjd186oModel.inc');
require_once('knjd186oQuery.inc');

class knjd186oController extends Controller {
    var $ModelClassName = "knjd186oModel";
    var $ProgramID      = "KNJD186O";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "gakki":
                    $sessionInstance->knjd186oModel();        //コントロールマスタの呼び出し
                    $this->callView("knjd186oForm1");
                    exit;
                case "knjd186o":                                //メニュー画面もしくはSUBMITした場合
                case "chgSeme":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $sessionInstance->knjd186oModel();        //コントロールマスタの呼び出し
                    $this->callView("knjd186oForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjd186oForm1");
                    }
                    $sessionInstance->setAccessLogDetail("EO", $ProgramID); 
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd186oCtl = new knjd186oController;
//var_dump($_REQUEST);
?>
