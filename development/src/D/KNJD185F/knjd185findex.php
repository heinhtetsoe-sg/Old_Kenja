<?php

require_once('for_php7.php');

require_once('knjd185fModel.inc');
require_once('knjd185fQuery.inc');

class knjd185fController extends Controller {
    var $ModelClassName = "knjd185fModel";
    var $ProgramID      = "KNJD185F";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "gakki":
                    $sessionInstance->knjd185fModel();        //コントロールマスタの呼び出し
                    $this->callView("knjd185fForm1");
                    exit;
                case "knjd185f":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $sessionInstance->knjd185fModel();        //コントロールマスタの呼び出し
                    $this->callView("knjd185fForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjd185fForm1");
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
$knjd185fCtl = new knjd185fController;
//var_dump($_REQUEST);
?>
