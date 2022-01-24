<?php

require_once('for_php7.php');

require_once('knjd155gModel.inc');
require_once('knjd155gQuery.inc');

class knjd155gController extends Controller {
    var $ModelClassName = "knjd155gModel";
    var $ProgramID      = "KNJD155G";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                    $sessionInstance->knjd155gModel();        //コントロールマスタの呼び出し
                    $this->callView("knjd155gForm1");
                    exit;
                case "change_grade":
                case "knjd155g":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $sessionInstance->knjd155gModel();        //コントロールマスタの呼び出し
                    $this->callView("knjd155gForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjd155gForm1");
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
$knjd155gCtl = new knjd155gController;
//var_dump($_REQUEST);
?>
