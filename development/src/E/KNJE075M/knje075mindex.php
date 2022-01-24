<?php

require_once('for_php7.php');

require_once('knje075mModel.inc');
require_once('knje075mQuery.inc');

class knje075mController extends Controller {
    var $ModelClassName = "knje075mModel";
    var $ProgramID      = "KNJE075M";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knje075m":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knje075mModel();      //コントロールマスタの呼び出し
                    $this->callView("knje075mForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knje075mForm1");
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
$knje075mCtl = new knje075mController;
//var_dump($_REQUEST);
?>
