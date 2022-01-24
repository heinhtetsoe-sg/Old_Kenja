<?php

require_once('for_php7.php');

require_once('knjd185iModel.inc');
require_once('knjd185iQuery.inc');

class knjd185iController extends Controller {
    var $ModelClassName = "knjd185iModel";
    var $ProgramID      = "KNJD185I";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "gakki":
                    $sessionInstance->knjd185iModel();        //コントロールマスタの呼び出し
                    $this->callView("knjd185iForm1");
                    exit;
                case "knjd185i":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $sessionInstance->knjd185iModel();        //コントロールマスタの呼び出し
                    $this->callView("knjd185iForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjd185iForm1");
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
$knjd185iCtl = new knjd185iController;
//var_dump($_REQUEST);
?>
