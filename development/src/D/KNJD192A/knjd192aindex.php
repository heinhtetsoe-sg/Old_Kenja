<?php

require_once('for_php7.php');

require_once('knjd192aModel.inc');
require_once('knjd192aQuery.inc');

class knjd192aController extends Controller {
    var $ModelClassName = "knjd192aModel";
    var $ProgramID      = "KNJD192A";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "change_grade":
                case "knjd192a":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjd192aModel();        //コントロールマスタの呼び出し
                    $this->callView("knjd192aForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjd192aForm1");
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
$knjd192aCtl = new knjd192aController;
//var_dump($_REQUEST);
?>
