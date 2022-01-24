<?php

require_once('for_php7.php');

require_once('knjm500kModel.inc');
require_once('knjm500kQuery.inc');

class knjm500kController extends Controller {
    var $ModelClassName = "knjm500kModel";
    var $ProgramID      = "KNJM500K";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "gakki":
                    $sessionInstance->knjm500kModel();        //コントロールマスタの呼び出し
                    $this->callView("knjm500kForm1");
                    exit;
                case "knjm500k":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $sessionInstance->knjm500kModel();        //コントロールマスタの呼び出し
                    $this->callView("knjm500kForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjm500kForm1");
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
$knjm500kCtl = new knjm500kController;
//var_dump($_REQUEST);
?>
