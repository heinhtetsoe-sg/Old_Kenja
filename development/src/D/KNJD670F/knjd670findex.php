<?php

require_once('for_php7.php');

require_once('knjd670fModel.inc');
require_once('knjd670fQuery.inc');

class knjd670fController extends Controller {
    var $ModelClassName = "knjd670fModel";
    var $ProgramID      = "KNJD670F";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd670f":                                //メニュー画面もしくはSUBMITした場合
                case "edit":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjd670fModel();        //コントロールマスタの呼び出し
                    $this->callView("knjd670fForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjd670fForm1");
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
$knjd670fCtl = new knjd670fController;
//var_dump($_REQUEST);
?>
