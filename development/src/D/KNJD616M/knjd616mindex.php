<?php

require_once('for_php7.php');

require_once('knjd616mModel.inc');
require_once('knjd616mQuery.inc');

class knjd616mController extends Controller {
    var $ModelClassName = "knjd616mModel";
    var $ProgramID      = "KNJD616M";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                    $sessionInstance->knjd616mModel();        //コントロールマスタの呼び出し
                    $this->callView("knjd616mForm1");
                    exit;
                case "knjd616m":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $sessionInstance->knjd616mModel();        //コントロールマスタの呼び出し
                    $this->callView("knjd616mForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjd616mForm1");
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
$knjd616mCtl = new knjd616mController;
//var_dump($_REQUEST);
?>
