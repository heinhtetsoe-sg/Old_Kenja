<?php

require_once('for_php7.php');

require_once('knjd616iModel.inc');
require_once('knjd616iQuery.inc');

class knjd616iController extends Controller {
    var $ModelClassName = "knjd616iModel";
    var $ProgramID      = "KNJD616I";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd616i":                                //メニュー画面もしくはSUBMITした場合
                case "edit":                                //メニュー画面もしくはSUBMITした場合
                case "change_grade":
                    $sessionInstance->knjd616iModel();        //コントロールマスタの呼び出し
                    $this->callView("knjd616iForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjd616iForm1");
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
$knjd616iCtl = new knjd616iController;
//var_dump($_REQUEST);
?>
