<?php

require_once('for_php7.php');

require_once('knjd617vModel.inc');
require_once('knjd617vQuery.inc');

class knjd617vController extends Controller {
    var $ModelClassName = "knjd617vModel";
    var $ProgramID      = "KNJD617V";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd617v":                                //メニュー画面もしくはSUBMITした場合
                case "edit":                                //メニュー画面もしくはSUBMITした場合
                case "change_grade":
                    $sessionInstance->knjd617vModel();        //コントロールマスタの呼び出し
                    $this->callView("knjd617vForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjd617vForm1");
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
$knjd617vCtl = new knjd617vController;
//var_dump($_REQUEST);
?>
