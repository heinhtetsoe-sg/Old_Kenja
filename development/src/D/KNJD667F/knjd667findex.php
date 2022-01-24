<?php

require_once('for_php7.php');

require_once('knjd667fModel.inc');
require_once('knjd667fQuery.inc');

class knjd667fController extends Controller {
    var $ModelClassName = "knjd667fModel";
    var $ProgramID      = "KNJD667F";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd667f":                                //メニュー画面もしくはSUBMITした場合
                case "edit":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjd667fModel();        //コントロールマスタの呼び出し
                    $this->callView("knjd667fForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjd667fForm1");
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
$knjd667fCtl = new knjd667fController;
//var_dump($_REQUEST);
?>
