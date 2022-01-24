<?php

require_once('for_php7.php');

require_once('knjd106fModel.inc');
require_once('knjd106fQuery.inc');

class knjd106fController extends Controller {
    var $ModelClassName = "knjd106fModel";
    var $ProgramID      = "KNJD106F";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd106f":                             //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $sessionInstance->knjd106fModel();       //コントロールマスタの呼び出し
                    $this->callView("knjd106fForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjd106fForm1");
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
$knjd106fCtl = new knjd106fController;
//var_dump($_REQUEST);
?>
