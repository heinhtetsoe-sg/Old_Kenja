<?php

require_once('for_php7.php');

require_once('knje130mModel.inc');
require_once('knje130mQuery.inc');

class knje130mController extends Controller {
    var $ModelClassName = "knje130mModel";
    var $ProgramID      = "KNJE130M";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knje130m":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knje130mModel();        //コントロールマスタの呼び出し
                    $this->callView("knje130mForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knje130mCtl = new knje130mController;
var_dump($_REQUEST);
?>
