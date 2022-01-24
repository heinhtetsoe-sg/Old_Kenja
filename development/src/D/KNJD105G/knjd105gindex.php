<?php

require_once('for_php7.php');

require_once('knjd105gModel.inc');
require_once('knjd105gQuery.inc');

class knjd105gController extends Controller {
    var $ModelClassName = "knjd105gModel";
    var $ProgramID      = "KNJD105G";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd105g":                                //メニュー画面もしくはSUBMITした場合
                case "edit":                                //メニュー画面もしくはSUBMITした場合
                case "change_grade":
                    $sessionInstance->knjd105gModel();        //コントロールマスタの呼び出し
                    $this->callView("knjd105gForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjd105gForm1");
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
$knjd105gCtl = new knjd105gController;
//var_dump($_REQUEST);
?>
