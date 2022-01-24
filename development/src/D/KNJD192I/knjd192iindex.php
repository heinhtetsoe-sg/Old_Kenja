<?php

require_once('for_php7.php');

require_once('knjd192iModel.inc');
require_once('knjd192iQuery.inc');

class knjd192iController extends Controller {
    var $ModelClassName = "knjd192iModel";
    var $ProgramID      = "KNJD192I";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                    $sessionInstance->knjd192iModel();        //コントロールマスタの呼び出し
                    $this->callView("knjd192iForm1");
                    exit;
                case "change_grade":
                case "knjd192i":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $sessionInstance->knjd192iModel();        //コントロールマスタの呼び出し
                    $this->callView("knjd192iForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjd192iForm1");
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
$knjd192iCtl = new knjd192iController;
//var_dump($_REQUEST);
?>
