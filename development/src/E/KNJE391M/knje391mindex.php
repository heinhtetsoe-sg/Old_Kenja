<?php

require_once('for_php7.php');

require_once('knje391mModel.inc');
require_once('knje391mQuery.inc');

class knje391mController extends Controller {
    var $ModelClassName = "knje391mModel";
    var $ProgramID      = "KNJE391M";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "clickchange":                         //メニュー画面もしくはSUBMITした場合
                case "knje391m":                             //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knje391mModel();       //コントロールマスタの呼び出し
                    $this->callView("knje391mForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knje391mCtl = new knje391mController;
//var_dump($_REQUEST);
?>
