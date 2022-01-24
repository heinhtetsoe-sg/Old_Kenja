<?php

require_once('for_php7.php');

require_once('knjd187iModel.inc');
require_once('knjd187iQuery.inc');

class knjd187iController extends Controller {
    var $ModelClassName = "knjd187iModel";
    var $ProgramID      = "KNJD187I";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "gakki":
                    $sessionInstance->knjd187iModel();        //コントロールマスタの呼び出し
                    $this->callView("knjd187iForm1");
                    exit;
                case "knjd187i":                                //メニュー画面もしくはSUBMITした場合
                case "chgSeme":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $sessionInstance->knjd187iModel();        //コントロールマスタの呼び出し
                    $this->callView("knjd187iForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjd187iForm1");
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
$knjd187iCtl = new knjd187iController;
//var_dump($_REQUEST);
?>
