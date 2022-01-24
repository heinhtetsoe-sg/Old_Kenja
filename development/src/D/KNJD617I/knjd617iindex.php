<?php

require_once('for_php7.php');

require_once('knjd617iModel.inc');
require_once('knjd617iQuery.inc');

class knjd617iController extends Controller {
    var $ModelClassName = "knjd617iModel";
    var $ProgramID      = "KNJD617I";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                    $sessionInstance->knjd617iModel();        //コントロールマスタの呼び出し
                    $this->callView("knjd617iForm1");
                    exit;
                case "knjd617i":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $sessionInstance->knjd617iModel();        //コントロールマスタの呼び出し
                    $this->callView("knjd617iForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjd617iForm1");
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
$knjd617iCtl = new knjd617iController;
//var_dump($_REQUEST);
?>
