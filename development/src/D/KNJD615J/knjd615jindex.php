<?php

require_once('for_php7.php');

require_once('knjd615jModel.inc');
require_once('knjd615jQuery.inc');

class knjd615jController extends Controller {
    var $ModelClassName = "knjd615jModel";
    var $ProgramID      = "KNJD615J";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd615j":                                //メニュー画面もしくはSUBMITした場合
                case "edit":                                //メニュー画面もしくはSUBMITした場合
                case "change_grade":
                    $sessionInstance->knjd615jModel();        //コントロールマスタの呼び出し
                    $this->callView("knjd615jForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjd615jForm1");
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
$knjd615jCtl = new knjd615jController;
//var_dump($_REQUEST);
?>
