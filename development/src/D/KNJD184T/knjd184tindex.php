<?php
require_once('knjd184tModel.inc');
require_once('knjd184tQuery.inc');

class knjd184tController extends Controller {
    var $ModelClassName = "knjd184tModel";
    var $ProgramID      = "KNJD184T";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "gakki":
                    $sessionInstance->knjd184tModel();        //コントロールマスタの呼び出し
                    $this->callView("knjd184tForm1");
                    exit;
                case "knjd184t":                                //メニュー画面もしくはSUBMITした場合
                case "chgSeme":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $sessionInstance->knjd184tModel();        //コントロールマスタの呼び出し
                    $this->callView("knjd184tForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjd184tForm1");
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
$knjd184tCtl = new knjd184tController;
//var_dump($_REQUEST);
?>
