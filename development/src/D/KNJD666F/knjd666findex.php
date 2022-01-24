<?php

require_once('for_php7.php');

require_once('knjd666fModel.inc');
require_once('knjd666fQuery.inc');

class knjd666fController extends Controller {
    var $ModelClassName = "knjd666fModel";
    var $ProgramID      = "KNJD666F";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd666f":                                //メニュー画面もしくはSUBMITした場合
                case "edit":                                //メニュー画面もしくはSUBMITした場合
                case "change_grade":
                    $sessionInstance->knjd666fModel();        //コントロールマスタの呼び出し
                    $this->callView("knjd666fForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjd666fForm1");
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
$knjd666fCtl = new knjd666fController;
//var_dump($_REQUEST);
?>
