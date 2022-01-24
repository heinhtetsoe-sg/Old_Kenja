<?php

require_once('for_php7.php');

require_once('knjj144jModel.inc');
require_once('knjj144jQuery.inc');

class knjj144jController extends Controller {
    var $ModelClassName = "knjj144jModel";
    var $ProgramID      = "KNJJ144J";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjj144j":                                //メニュー画面もしくはSUBMITした場合
                case "edit":                                //メニュー画面もしくはSUBMITした場合
                case "change_grade":
                case "change_semes":
                    $sessionInstance->knjj144jModel();        //コントロールマスタの呼び出し
                    $this->callView("knjj144jForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjj144jForm1");
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
$knjj144jCtl = new knjj144jController;
//var_dump($_REQUEST);
?>
