<?php

require_once('for_php7.php');

require_once('knjd615iModel.inc');
require_once('knjd615iQuery.inc');

class knjd615iController extends Controller {
    var $ModelClassName = "knjd615iModel";
    var $ProgramID      = "KNJD615I";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd615i":                                //メニュー画面もしくはSUBMITした場合
                case "edit":                                //メニュー画面もしくはSUBMITした場合
                case "change_grade":
                    $sessionInstance->knjd615iModel();        //コントロールマスタの呼び出し
                    $this->callView("knjd615iForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjd615iForm1");
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
$knjd615iCtl = new knjd615iController;
//var_dump($_REQUEST);
?>
