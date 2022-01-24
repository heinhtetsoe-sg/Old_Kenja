<?php

require_once('for_php7.php');

require_once('knjb151tModel.inc');
require_once('knjb151tQuery.inc');

class knjb151tController extends Controller {
    var $ModelClassName = "knjb151tModel";
    var $ProgramID      = "KNJB151T";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjb151tForm1");
                    }
                    $sessionInstance->setAccessLogDetail("EO", $ProgramID); 
                    break 2;
                case "":
                case "knjb151t":                              //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $sessionInstance->knjb151tModel();        //コントロールマスタの呼び出し
                    $this->callView("knjb151tForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }

    }
}
$knjb151tCtl = new knjb151tController;
//var_dump($_REQUEST);
?>
