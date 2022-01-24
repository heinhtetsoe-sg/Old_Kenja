<?php

require_once('for_php7.php');

require_once('knjd185mModel.inc');
require_once('knjd185mQuery.inc');

class knjd185mController extends Controller {
    var $ModelClassName = "knjd185mModel";
    var $ProgramID      = "KNJD185M";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "gakki":
                case "knjd185m":                             //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjd185mModel();       //コントロールマスタの呼び出し
                    $this->callView("knjd185mForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjd185mForm1");
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
$knjd185mCtl = new knjd185mController;
//var_dump($_REQUEST);
?>
