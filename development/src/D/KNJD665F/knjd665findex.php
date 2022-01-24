<?php

require_once('for_php7.php');

require_once('knjd665fModel.inc');
require_once('knjd665fQuery.inc');

class knjd665fController extends Controller {
    var $ModelClassName = "knjd665fModel";
    var $ProgramID      = "KNJD665F";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd665f":                                //メニュー画面もしくはSUBMITした場合
                case "edit":                                //メニュー画面もしくはSUBMITした場合
                case "change_grade":
                    $sessionInstance->knjd665fModel();        //コントロールマスタの呼び出し
                    $this->callView("knjd665fForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjd665fForm1");
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
$knjd665fCtl = new knjd665fController;
//var_dump($_REQUEST);
?>
