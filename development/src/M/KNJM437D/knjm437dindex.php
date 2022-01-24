<?php

require_once('for_php7.php');

require_once('knjm437dModel.inc');
require_once('knjm437dQuery.inc');

class knjm437dController extends Controller {
    var $ModelClassName = "knjm437dModel";
    var $ProgramID      = "KNJM437D";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "gakki":
                case "knjm437d":                             //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjm437dModel();       //コントロールマスタの呼び出し
                    $this->callView("knjm437dForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjm437dForm1");
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
$knjm437dCtl = new knjm437dController;
//var_dump($_REQUEST);
?>
