<?php
require_once('knjd175dModel.inc');
require_once('knjd175dQuery.inc');

class knjd175dController extends Controller {
    var $ModelClassName = "knjd175dModel";
    var $ProgramID      = "KNJD175D";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "gakki":
                case "knjd175d":                             //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjd175dModel();       //コントロールマスタの呼び出し
                    $this->callView("knjd175dForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjd175dForm1");
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
$knjd175dCtl = new knjd175dController;
//var_dump($_REQUEST);
?>
