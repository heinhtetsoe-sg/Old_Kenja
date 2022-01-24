<?php

require_once('for_php7.php');

require_once('knjd154iModel.inc');
require_once('knjd154iQuery.inc');

class knjd154iController extends Controller {
    var $ModelClassName = "knjd154iModel";
    var $ProgramID      = "KNJD154I";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd154i":                                //メニュー画面もしくはSUBMITした場合
                case "edit":                                //メニュー画面もしくはSUBMITした場合
                case "change_grade":
                case "change_semes":
                    $sessionInstance->knjd154iModel();        //コントロールマスタの呼び出し
                    $this->callView("knjd154iForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knjd154iForm1");
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
$knjd154iCtl = new knjd154iController;
//var_dump($_REQUEST);
?>
