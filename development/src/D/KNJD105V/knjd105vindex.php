<?php

require_once('for_php7.php');

require_once('knjd105vModel.inc');
require_once('knjd105vQuery.inc');

class knjd105vController extends Controller {
    var $ModelClassName = "knjd105vModel";
    var $ProgramID      = "KNJD105V";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd105v":                                //メニュー画面もしくはSUBMITした場合
                case "edit":                                //メニュー画面もしくはSUBMITした場合
                case "change_grade":
                    $sessionInstance->knjd105vModel();        //コントロールマスタの呼び出し
                    $this->callView("knjd105vForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjd105vForm1");
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
$knjd105vCtl = new knjd105vController;
//var_dump($_REQUEST);
?>
