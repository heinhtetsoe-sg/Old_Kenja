<?php

require_once('for_php7.php');

require_once('knjd617gModel.inc');
require_once('knjd617gQuery.inc');

class knjd617gController extends Controller {
    var $ModelClassName = "knjd617gModel";
    var $ProgramID      = "KNJD617G";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd617g":                                //メニュー画面もしくはSUBMITした場合
                case "edit":                                //メニュー画面もしくはSUBMITした場合
                case "change_grade":
                    $sessionInstance->knjd617gModel();        //コントロールマスタの呼び出し
                    $this->callView("knjd617gForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjd617gForm1");
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
$knjd617gCtl = new knjd617gController;
//var_dump($_REQUEST);
?>
