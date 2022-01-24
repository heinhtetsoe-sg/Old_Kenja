<?php

require_once('for_php7.php');

require_once('knjd617aModel.inc');
require_once('knjd617aQuery.inc');

class knjd617aController extends Controller {
    var $ModelClassName = "knjd617aModel";
    var $ProgramID      = "KNJD617A";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd617a":                                //メニュー画面もしくはSUBMITした場合
                case "edit":                                //メニュー画面もしくはSUBMITした場合
                case "change_grade":
                    $sessionInstance->knjd617aModel();        //コントロールマスタの呼び出し
                    $this->callView("knjd617aForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjd617aForm1");
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
$knjd617aCtl = new knjd617aController;
//var_dump($_REQUEST);
?>
