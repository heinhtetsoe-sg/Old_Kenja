<?php

require_once('for_php7.php');

require_once('knjd185oModel.inc');
require_once('knjd185oQuery.inc');

class knjd185oController extends Controller {
    var $ModelClassName = "knjd185oModel";
    var $ProgramID      = "KNJD185O";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "gakki":
                case "knjd185o":                             //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjd185oModel();       //コントロールマスタの呼び出し
                    $this->callView("knjd185oForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjd185oForm1");
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
$knjd185oCtl = new knjd185oController;
//var_dump($_REQUEST);
?>
