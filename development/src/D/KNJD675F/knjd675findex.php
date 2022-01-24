<?php

require_once('for_php7.php');

require_once('knjd675fModel.inc');
require_once('knjd675fQuery.inc');

class knjd675fController extends Controller {
    var $ModelClassName = "knjd675fModel";
    var $ProgramID      = "KNJD675F";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd675f":                             //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $sessionInstance->knjd675fModel();       //コントロールマスタの呼び出し
                    $this->callView("knjd675fForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjd675fForm1");
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
$knjd675fCtl = new knjd675fController;
//var_dump($_REQUEST);
?>
