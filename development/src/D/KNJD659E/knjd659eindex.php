<?php

require_once('for_php7.php');

require_once('knjd659eModel.inc');
require_once('knjd659eQuery.inc');

class knjd659eController extends Controller {
    var $ModelClassName = "knjd659eModel";
    var $ProgramID      = "KNJD659E";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd659e":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjd659eModel();        //コントロールマスタの呼び出し
                    $this->callView("knjd659eForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjd659eForm1");
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
$knjd659eCtl = new knjd659eController;
//var_dump($_REQUEST);
?>
