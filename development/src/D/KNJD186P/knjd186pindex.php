<?php

require_once('for_php7.php');

require_once('knjd186pModel.inc');
require_once('knjd186pQuery.inc');

class knjd186pController extends Controller {
    var $ModelClassName = "knjd186pModel";
    var $ProgramID      = "KNJD186P";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "semester":
                case "knjd186p":                             //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjd186pModel();       //コントロールマスタの呼び出し
                    $this->callView("knjd186pForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjd186pForm1");
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
$knjd186pCtl = new knjd186pController;
//var_dump($_REQUEST);
?>
