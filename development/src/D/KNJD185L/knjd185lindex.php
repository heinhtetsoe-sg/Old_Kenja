<?php

require_once('for_php7.php');

require_once('knjd185lModel.inc');
require_once('knjd185lQuery.inc');

class knjd185lController extends Controller {
    var $ModelClassName = "knjd185lModel";
    var $ProgramID      = "KNJD185L";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "gakki":
                    $sessionInstance->knjd185lModel();        //コントロールマスタの呼び出し
                    $this->callView("knjd185lForm1");
                    exit;
                case "knjd185l":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $sessionInstance->knjd185lModel();        //コントロールマスタの呼び出し
                    $this->callView("knjd185lForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjd185lForm1");
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
$knjd185lCtl = new knjd185lController;
//var_dump($_REQUEST);
?>
