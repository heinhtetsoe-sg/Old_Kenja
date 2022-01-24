<?php

require_once('for_php7.php');

require_once('knjd659fModel.inc');
require_once('knjd659fQuery.inc');

class knjd659fController extends Controller {
    var $ModelClassName = "knjd659fModel";
    var $ProgramID      = "KNJD659F";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd659f":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjd659fModel();        //コントロールマスタの呼び出し
                    $this->callView("knjd659fForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjd659fForm1");
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
$knjd659fCtl = new knjd659fController;
//var_dump($_REQUEST);
?>
