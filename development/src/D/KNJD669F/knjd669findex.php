<?php

require_once('for_php7.php');

require_once('knjd669fModel.inc');
require_once('knjd669fQuery.inc');

class knjd669fController extends Controller {
    var $ModelClassName = "knjd669fModel";
    var $ProgramID      = "KNJD669F";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd669f":                                //メニュー画面もしくはSUBMITした場合
                case "edit":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjd669fModel();        //コントロールマスタの呼び出し
                    $this->callView("knjd669fForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjd669fForm1");
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
$knjd669fCtl = new knjd669fController;
//var_dump($_REQUEST);
?>
