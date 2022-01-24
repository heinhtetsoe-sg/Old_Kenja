<?php

require_once('for_php7.php');

require_once('knjd186qModel.inc');
require_once('knjd186qQuery.inc');

class knjd186qController extends Controller {
    var $ModelClassName = "knjd186qModel";
    var $ProgramID      = "KNJD186Q";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "gakki":
                case "knjd186q":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjd186qModel();        //コントロールマスタの呼び出し
                    $this->callView("knjd186qForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjd186qForm1");
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
$knjd186qCtl = new knjd186qController;
//var_dump($_REQUEST);
?>
