<?php

require_once('for_php7.php');

require_once('knjd625eModel.inc');
require_once('knjd625eQuery.inc');

class knjd625eController extends Controller {
    var $ModelClassName = "knjd625eModel";
    var $ProgramID      = "KNJD625E";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "gakki":
                case "knjd625e":                             //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjd625eModel();       //コントロールマスタの呼び出し
                    $this->callView("knjd625eForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjd625eForm1");
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
$knjd625eCtl = new knjd625eController;
//var_dump($_REQUEST);
?>
