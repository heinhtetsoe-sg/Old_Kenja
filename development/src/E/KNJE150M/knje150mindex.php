<?php

require_once('for_php7.php');

require_once('knje150mModel.inc');
require_once('knje150mQuery.inc');

class knje150mController extends Controller {
    var $ModelClassName = "knje150mModel";
    var $ProgramID      = "KNJE150M";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knje150m":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knje150mModel();        //コントロールマスタの呼び出し
                    $this->callView("knje150mForm1");
                    exit;
                case "csv":     //CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()){
                        $this->callView("knje150mForm1");
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
$knje150mCtl = new knje150mController;
//var_dump($_REQUEST);
?>
