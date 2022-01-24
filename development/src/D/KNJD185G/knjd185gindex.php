<?php

require_once('for_php7.php');

require_once('knjd185gModel.inc');
require_once('knjd185gQuery.inc');

class knjd185gController extends Controller {
    var $ModelClassName = "knjd185gModel";
    var $ProgramID      = "KNJD185G";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "gakki":
                    $sessionInstance->knjd185gModel();        //コントロールマスタの呼び出し
                    $this->callView("knjd185gForm1");
                    exit;
                case "knjd185g":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $sessionInstance->knjd185gModel();        //コントロールマスタの呼び出し
                    $this->callView("knjd185gForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjd185gForm1");
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
$knjd185gCtl = new knjd185gController;
//var_dump($_REQUEST);
?>
