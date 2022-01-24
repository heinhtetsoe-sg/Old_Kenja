<?php

require_once('for_php7.php');

require_once('knjd192bModel.inc');
require_once('knjd192bQuery.inc');

class knjd192bController extends Controller {
    var $ModelClassName = "knjd192bModel";
    var $ProgramID      = "KNJD192B";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd192b":                                //メニュー画面もしくはSUBMITした場合
                case "change_grade":
                    $sessionInstance->knjd192bModel();        //コントロールマスタの呼び出し
                    $this->callView("knjd192bForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjd192bForm1");
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
$knjd192bCtl = new knjd192bController;
?>
